package ofis.tool.image.png.optimize.service

import ofis.tool.pdf.compress.model.CompressionError
import ofis.tool.pdf.compress.model.CompressionResult
import ofis.tool.pdf.compress.model.ProcessResult
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PngOptimizeServiceTest {
    private val fs = FileSystem.SYSTEM
    private val tmpDir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ofis_test_png"
    private val input = tmpDir / "input.png"
    private val output = tmpDir / "output.png"

    @BeforeTest
    fun setUp() {
        fs.createDirectories(tmpDir)
        fs.write(input) { write(ByteArray(INPUT_SIZE)) }
    }

    @AfterTest
    fun tearDown() {
        fs.deleteRecursively(tmpDir, mustExist = false)
    }

    @Test
    fun `keeps a smaller output`() {
        val result = assertIs<CompressionResult.Success>(serviceWriting(INPUT_SIZE / 2).optimize(input, output))

        assertEquals(output, result.outputPath)
        assertEquals((INPUT_SIZE / 2).toLong(), result.compressedBytes)
    }

    @Test
    fun `drops an output that is not smaller and points at the input`() {
        val result = assertIs<CompressionResult.Success>(serviceWriting(INPUT_SIZE).optimize(input, output))

        assertEquals(input, result.outputPath)
        assertTrue(result.alreadyOptimal)
        assertFalse(fs.exists(output))
    }

    @Test
    fun `refuses output equal to input and leaves the file alone`() {
        val result = assertIs<CompressionResult.Failure>(serviceWriting(INPUT_SIZE).optimize(input, input))

        assertIs<CompressionError.InvalidInput>(result.reason)
        assertEquals(INPUT_SIZE.toLong(), fs.metadata(input).size)
    }

    @Test
    fun `reports a missing binary`() {
        val service = PngOptimizeService(binaryFinder = { null }, processRunner = { error("must not run") })

        val result = assertIs<CompressionResult.Failure>(service.optimize(input, output))

        assertEquals(CompressionError.BinaryNotFound("oxipng"), result.reason)
    }

    @Test
    fun `reports a failed run with its stderr`() {
        val service =
            PngOptimizeService(
                binaryFinder = { "oxipng" },
                processRunner = { ProcessResult(1, "", "not a PNG") },
            )

        val result = assertIs<CompressionResult.Failure>(service.optimize(input, output))

        assertEquals(CompressionError.ProcessFailed("oxipng", 1, "not a PNG"), result.reason)
    }

    @Test
    fun `passes the input after the end of options marker`() {
        val command = buildOxipngCommand("oxipng", "-odd.png".toPath(), output)

        assertEquals(listOf("--", "-odd.png"), command.arguments.takeLast(2))
    }

    private fun serviceWriting(outputSize: Int) =
        PngOptimizeService(
            binaryFinder = { "oxipng" },
            processRunner = {
                fs.write(output) { write(ByteArray(outputSize)) }
                ProcessResult(0, "", "")
            },
        )

    private companion object {
        const val INPUT_SIZE = 1024
    }
}
