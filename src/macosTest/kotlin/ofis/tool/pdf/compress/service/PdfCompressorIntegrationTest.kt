package ofis.tool.pdf.compress.service

import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.model.CompressionRequest
import ofis.tool.pdf.compress.model.CompressionResult
import okio.FileSystem
import okio.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Integration tests: run actual qpdf + ghostscript binaries against real-ish PDFs.
 *
 * Prerequisites (on CI add to workflow: `brew install qpdf ghostscript`):
 *   make install-bins
 *
 * PDFs are generated programmatically — no binary fixtures committed, no network.
 * All output files are cleaned up after each test.
 */
class PdfCompressorIntegrationTest {
    private val fs = FileSystem.SYSTEM
    private val tmpDir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ofis_integration_test"

    private val textPdf: Path get() = tmpDir / "text.pdf"
    private val photoPdf: Path get() = tmpDir / "photo.pdf"

    private val outputs = mutableListOf<Path>()

    @BeforeTest
    fun setUp() {
        fs.createDirectories(tmpDir)
        fs.write(textPdf) { write(syntheticTextPdf()) }
        fs.write(photoPdf) { write(syntheticPhotoPdf()) }
        println()
        println("[integration] text PDF:  ${fs.metadata(textPdf).size?.div(1024)} KB")
        println("[integration] photo PDF: ${fs.metadata(photoPdf).size?.div(1024)} KB")
    }

    @AfterTest
    fun tearDown() {
        outputs.forEach { fs.delete(it, mustExist = false) }
        outputs.clear()
        listOf(textPdf, photoPdf).forEach { fs.delete(it, mustExist = false) }
        fs.deleteRecursively(tmpDir, mustExist = false)
    }

    // ── text PDF — qpdf recompresses streams ──────────────────────────────────

    @Test
    fun `HIGH_QUALITY - text PDF - qpdf only`() {
        compress(textPdf, CompressionProfile.HIGH_QUALITY).let { reportAndAssert("HIGH_QUALITY text", it) }
    }

    @Test
    fun `BALANCED - text PDF - qpdf then ghostscript`() {
        compress(textPdf, CompressionProfile.BALANCED).let { reportAndAssert("BALANCED text", it) }
    }

    @Test
    fun `MAXIMUM - text PDF - qpdf then ghostscript lowest DPI`() {
        compress(textPdf, CompressionProfile.MAXIMUM).let { reportAndAssert("MAXIMUM text", it) }
    }

    // ── photo PDF — ghostscript downsamples raster images ─────────────────────

    @Test
    fun `HIGH_QUALITY - photo PDF - qpdf only`() {
        compress(photoPdf, CompressionProfile.HIGH_QUALITY).let { reportAndAssert("HIGH_QUALITY photo", it) }
    }

    @Test
    fun `BALANCED - photo PDF - ghostscript downsamples to 144 DPI`() {
        val result = compress(photoPdf, CompressionProfile.BALANCED)
        reportAndAssert("BALANCED photo", result)
        // Ghostscript should shrink the embedded high-res image noticeably
        assertIs<CompressionResult.Success>(result)
        assertTrue(
            result.compressedBytes < result.originalBytes,
            "Expected BALANCED to shrink photo PDF but got ${result.compressedBytes}B >= ${result.originalBytes}B",
        )
    }

    @Test
    fun `MAXIMUM - photo PDF - ghostscript downsamples to 96 DPI`() {
        val result = compress(photoPdf, CompressionProfile.MAXIMUM)
        reportAndAssert("MAXIMUM photo", result)
        assertIs<CompressionResult.Success>(result)
        assertTrue(
            result.compressedBytes < result.originalBytes,
            "Expected MAXIMUM to shrink photo PDF but got ${result.compressedBytes}B >= ${result.originalBytes}B",
        )
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun compress(
        input: Path,
        profile: CompressionProfile,
    ): CompressionResult {
        val output = (tmpDir / "out_${input.name}_${profile.name.lowercase()}.pdf").also { outputs += it }
        val service = PdfCompressionService()
        var result: CompressionResult? = null
        val elapsed =
            measureTime {
                result = service.compress(CompressionRequest(inputPath = input, outputPath = output, profile = profile))
            }
        println("[integration] ${profile.name} ${input.name} wall-clock: $elapsed")
        return result!!
    }

    private fun reportAndAssert(
        label: String,
        result: CompressionResult,
    ) {
        assertIs<CompressionResult.Success>(result, "Expected success, got: $result")
        val origKb = result.originalBytes / 1024
        val compKb = result.compressedBytes / 1024
        println("[integration] $label: ${origKb}KB → ${compKb}KB (${result.savedPercent}% saved, alreadyOptimal=${result.alreadyOptimal})")
        assertTrue(result.compressedBytes > 0, "Output must not be empty")
        assertTrue(fs.exists(result.outputPath), "Output file must exist")
    }
}

// ── PDF generators ─────────────────────────────────────────────────────────────

/**
 * Minimal multi-page PDF with uncompressed text streams.
 * qpdf's flate recompression achieves meaningful savings on repetitive text.
 */
private fun syntheticTextPdf(): ByteArray {
    val line = "The quick brown fox jumps over the lazy dog. Ofis PDF compression test. 1234567890"
    val pageContent = (1..120).joinToString("\n") { i -> "$i: $line" }
    val stream =
        "BT /F1 10 Tf 40 750 Td 12 TL\n" +
            pageContent.lines().joinToString("\n") { ln ->
                "(${ln.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")}) Tj T*"
            } + "\nET"
    return buildPdf(numPages = 5, contentStream = stream, includeFont = true)
}

/**
 * Single-page PDF with an embedded 300×300 RGB raster image (uncompressed, ASCIIHex encoded).
 * Simulates a scanned document / photo. BALANCED and MAXIMUM profiles will have ghostscript
 * downsample the image, producing real space savings.
 */
private fun syntheticPhotoPdf(): ByteArray {
    val w = 300
    val h = 300
    val pixels = ByteArray(w * h * 3)

    // Pseudo-random gradient + noise to simulate a natural photo (high entropy)
    var seed = 0x12345678L

    fun rand(): Int {
        seed = seed * 6364136223846793005L + 1442695040888963407L
        return ((seed ushr 33) and 0xFFL).toInt()
    }
    for (y in 0 until h) {
        for (x in 0 until w) {
            val i = (y * w + x) * 3
            val r = (x * 255 / w + rand() / 4) and 0xFF
            val g = (y * 255 / h + rand() / 4) and 0xFF
            val b = (rand() + rand()) / 2 and 0xFF
            pixels[i] = r.toByte()
            pixels[i + 1] = g.toByte()
            pixels[i + 2] = b.toByte()
        }
    }

    // Encode as ASCII hex — stays inside a Kotlin String, no binary stream issues
    val hex =
        buildString(pixels.size * 2 + 1) {
            val digits = "0123456789ABCDEF"
            for (b in pixels) {
                val v = b.toInt() and 0xFF
                append(digits[v ushr 4])
                append(digits[v and 0xF])
            }
            append('>') // ASCIIHexDecode terminator
        }

    val imageObj =
        "6 0 obj\n" +
            "<< /Type /XObject /Subtype /Image\n" +
            "   /Width $w /Height $h\n" +
            "   /ColorSpace /DeviceRGB /BitsPerComponent 8\n" +
            "   /Filter /ASCIIHexDecode /Length ${hex.length}\n" +
            ">>\nstream\n" +
            hex +
            "\nendstream\nendobj\n"

    // Place the image filling most of the page
    val contentStream = "q ${w * 2} 0 0 ${h * 2} 56 100 cm /Im1 Do Q"

    return buildPdf(
        numPages = 1,
        contentStream = contentStream,
        includeFont = false,
        extraObjs = listOf(imageObj),
        extraResources = "/XObject << /Im1 6 0 R >>",
    )
}

// ── PDF assembler ──────────────────────────────────────────────────────────────

private fun buildPdf(
    numPages: Int,
    contentStream: String,
    includeFont: Boolean,
    extraObjs: List<String> = emptyList(),
    extraResources: String = "",
): ByteArray {
    val sb = StringBuilder()
    sb.append("%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n")

    val offsets = mutableMapOf<Int, Int>()

    fun obj(
        id: Int,
        body: String,
        isStream: Boolean = false,
    ) {
        offsets[id] = sb.length
        if (isStream) {
            sb.append("$id 0 obj\n<< /Length ${body.length} >>\nstream\n$body\nendstream\nendobj\n")
        } else {
            sb.append("$id 0 obj\n$body\nendobj\n")
        }
    }

    var nextId = 1

    // Font (optional)
    val fontId: Int? =
        if (includeFont) {
            val id = nextId++
            obj(id, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")
            id
        } else {
            null
        }

    // Extra objects (e.g. image XObjects) — pre-numbered with literal IDs in their bodies
    for (extra in extraObjs) {
        offsets[nextId] = sb.length
        sb.append(extra)
        nextId++
    }

    // Resources
    val resourcesId = nextId++
    val fontResource = if (fontId != null) "/Font << /F1 $fontId 0 R >>" else ""
    obj(resourcesId, "<< $fontResource $extraResources >>")

    // Content stream (one shared stream; each page references it)
    val contentId = nextId++
    obj(contentId, contentStream, isStream = true)

    // Page objects
    val pagesId = nextId + numPages // will be defined after pages
    val pageIds = (0 until numPages).map { nextId++ }
    for (pid in pageIds) {
        obj(pid, "<< /Type /Page /Parent $pagesId 0 R /MediaBox [0 0 612 792] /Contents $contentId 0 R /Resources $resourcesId 0 R >>")
    }

    // Pages
    val actualPagesId = nextId++
    check(actualPagesId == pagesId)
    obj(pagesId, "<< /Type /Pages /Count $numPages /Kids [${pageIds.joinToString(" ") { "$it 0 R" }}] >>")

    // Catalog
    val catalogId = nextId++
    obj(catalogId, "<< /Type /Catalog /Pages $pagesId 0 R >>")

    val totalObjs = catalogId
    val xrefOffset = sb.length
    sb.append("xref\n0 ${totalObjs + 1}\n")
    sb.append("0000000000 65535 f \n")
    for (i in 1..totalObjs) {
        sb.append("${(offsets[i] ?: 0).toString().padStart(10, '0')} 00000 n \n")
    }
    sb.append("trailer\n<< /Size ${totalObjs + 1} /Root $catalogId 0 R >>\nstartxref\n$xrefOffset\n%%EOF\n")

    return sb.toString().encodeToByteArray()
}
