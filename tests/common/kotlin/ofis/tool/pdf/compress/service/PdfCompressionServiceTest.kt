package ofis.tool.pdf.compress.service

import ofis.tool.pdf.compress.model.CompressionError
import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.model.CompressionRequest
import ofis.tool.pdf.compress.model.CompressionResult
import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PdfCompressionServiceTest {
    private val fs = FileSystem.SYSTEM
    private val tmpDir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ofis_test_compress"

    private val inputPdf: Path get() = tmpDir / "input.pdf"
    private val outputPdf: Path get() = tmpDir / "output.pdf"

    @BeforeTest
    fun setUp() {
        fs.createDirectories(tmpDir)
        // Minimal valid-ish PDF bytes (real tools would reject this, but our fakes won't read it)
        fs.write(inputPdf) { write(ByteArray(1024) { it.toByte() }) }
    }

    @AfterTest
    fun tearDown() {
        fs.deleteRecursively(tmpDir, mustExist = false)
    }

    // ── binary-not-found ──────────────────────────────────────────────────────

    @Test
    fun `returns BinaryNotFound when qpdf is missing`() {
        val service = service(binaryFinder = { null })

        val result = service.compress(request(CompressionProfile.HIGH_QUALITY))

        assertIs<CompressionResult.Failure>(result)
        assertIs<CompressionError.BinaryNotFound>((result).reason)
        assertEquals("qpdf", (result.reason as CompressionError.BinaryNotFound).name)
    }

    @Test
    fun `returns BinaryNotFound for gs when qpdf succeeds but gs is missing`() {
        val service =
            service(
                binaryFinder = { name -> if (name == "qpdf") "/usr/bin/qpdf" else null },
                processRunner = { cmd -> fakeSuccess(cmd, tmpDir) },
            )

        val result = service.compress(request(CompressionProfile.BALANCED))

        assertIs<CompressionResult.Failure>(result)
        assertIs<CompressionError.BinaryNotFound>(result.reason)
        assertTrue((result.reason as CompressionError.BinaryNotFound).name.contains("gs"))
    }

    // ── process failure ────────────────────────────────────────────────────────

    @Test
    fun `returns ProcessFailed when qpdf exits non-zero`() {
        val service =
            service(
                binaryFinder = { "/fake/qpdf" },
                processRunner = { ProcessResult(exitCode = 1, stdout = "", stderr = "bad pdf") },
            )

        val result = service.compress(request(CompressionProfile.HIGH_QUALITY))

        assertIs<CompressionResult.Failure>(result)
        val err = assertIs<CompressionError.ProcessFailed>(result.reason)
        assertEquals("qpdf", err.tool)
        assertEquals(1, err.exitCode)
    }

    @Test
    fun `exit code 127 renders as not-installed message`() {
        val err = CompressionError.ProcessFailed("qpdf", 127, "")
        assertTrue(err.toString().contains("not installed"))
    }

    // ── success paths ──────────────────────────────────────────────────────────

    @Test
    fun `HIGH_QUALITY profile skips ghostscript`() {
        val calledWith = mutableListOf<String>()
        val service =
            service(
                binaryFinder = { name -> "/fake/$name" },
                processRunner = { cmd ->
                    calledWith += cmd.executable
                    fakeSuccess(cmd, tmpDir)
                },
            )

        service.compress(request(CompressionProfile.HIGH_QUALITY))

        assertEquals(1, calledWith.size, "Only qpdf should run for HIGH_QUALITY")
        assertTrue(calledWith.first().contains("qpdf"))
    }

    @Test
    fun `BALANCED profile runs qpdf then ghostscript`() {
        val calledWith = mutableListOf<String>()
        val service =
            service(
                binaryFinder = { name -> "/fake/$name" },
                processRunner = { cmd ->
                    calledWith += cmd.executable
                    fakeSuccess(cmd, tmpDir)
                },
            )

        service.compress(request(CompressionProfile.BALANCED))

        assertEquals(2, calledWith.size, "qpdf + gs should run for BALANCED")
    }

    @Test
    fun `returns Success with correct size metrics`() {
        val originalSize = 1024L
        // fake: write 512 bytes to output — simulates 50% compression
        val service =
            service(
                binaryFinder = { "/fake/$it" },
                processRunner = { cmd ->
                    val output = cmd.arguments.last().toPath()
                    fs.write(output) { write(ByteArray(512)) }
                    ProcessResult(exitCode = 0, stdout = "", stderr = "")
                },
            )

        val result = service.compress(request(CompressionProfile.HIGH_QUALITY))

        assertIs<CompressionResult.Success>(result)
        assertEquals(originalSize, result.originalBytes)
        assertEquals(512L, result.compressedBytes)
        assertEquals(50, result.savedPercent)
    }

    @Test
    fun `already-optimal keeps original when compressed is not smaller`() {
        // fake: write more bytes than the input — service should keep original
        val service =
            service(
                binaryFinder = { "/fake/$it" },
                processRunner = { cmd ->
                    val output = cmd.arguments.last().toPath()
                    fs.write(output) { write(ByteArray(4096)) }
                    ProcessResult(exitCode = 0, stdout = "", stderr = "")
                },
            )

        val result = service.compress(request(CompressionProfile.HIGH_QUALITY))

        assertIs<CompressionResult.Success>(result)
        assertTrue(result.alreadyOptimal)
        assertEquals(result.inputPath, result.outputPath)
    }

    @Test
    fun `qpdf exit code 3 is treated as success with warning`() {
        val service =
            service(
                binaryFinder = { "/fake/$it" },
                processRunner = { cmd ->
                    val output = cmd.arguments.last().toPath()
                    fs.write(output) { write(ByteArray(512)) }
                    ProcessResult(exitCode = 3, stdout = "", stderr = "")
                },
            )

        val result = service.compress(request(CompressionProfile.HIGH_QUALITY))

        assertIs<CompressionResult.Success>(result)
        assertTrue(result.warnings.any { it.contains("warnings") })
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun request(profile: CompressionProfile) =
        CompressionRequest(
            inputPath = inputPdf,
            outputPath = outputPdf,
            profile = profile,
        )

    private fun service(
        binaryFinder: (String) -> String? = { "/fake/$it" },
        processRunner: (NativeCommand) -> ProcessResult = { fakeSuccess(it, tmpDir) },
    ) = PdfCompressionService(binaryFinder = binaryFinder, processRunner = processRunner)

    /**
     * Writes 512 bytes to the output path and returns exit code 0.
     * Handles both qpdf (last arg is output) and ghostscript (-sOutputFile=<path>).
     */
    private fun fakeSuccess(
        cmd: NativeCommand,
        @Suppress("UNUSED_PARAMETER") unused: Path,
    ): ProcessResult {
        val outputPath =
            cmd.arguments
                .firstOrNull { it.startsWith("-sOutputFile=") }
                ?.substringAfter("-sOutputFile=")
                ?.toPath()
                ?: cmd.arguments.last().toPath()
        fs.write(outputPath) { write(ByteArray(512)) }
        return ProcessResult(exitCode = 0, stdout = "", stderr = "")
    }
}
