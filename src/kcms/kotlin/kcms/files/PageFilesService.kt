package kcms.files

import com.google.common.cache.CacheBuilder
import kcms.common.Caches
import kcms.common.Caching
import kcms.common.CommonService
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartFile
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.imageio.ImageIO

@Service
class PageFilesService(
    val pageFileRepository: PageFileRepository,
    val scales: List<KcmsImageScale>,
) : CommonService(), Caching {

    private val filesByPageCache = CacheBuilder.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .softValues()
        .build<Long, List<PageFile>>()

    override fun resetCaches() {
        filesByPageCache.invalidateAll()
    }

    fun getPageFiles(pageId: Long): List<PageFile> = filesByPageCache.get(pageId) {
        pageFileRepository.findByPageId(pageId).sortedBy { it.order }.map { it.copy() }
    }

    fun save(pageId: Long?, file: MultipartFile) {
        val origName = file.originalFilename ?: ""

        val type = PageFileType.values().firstOrNull { t ->
            t.ext.any { e -> origName.endsWith(".$e", ignoreCase = true) }
        } ?: return

        val tempFile = File("files/${UUID.randomUUID()}").absoluteFile
        tempFile.parentFile.mkdirs()
        file.transferTo(tempFile)

        val f = PageFile(
            id = pageFileRepository.nextId(),
            pageId = pageId,
            type = type,
            origName = origName,
            size = tempFile.length()
        )

        tempFile.renameTo(File(".${f.url()}").absoluteFile.also {
            it.parentFile.mkdirs()
        })

        pageFileRepository.save(f)

        Caches.instance.reset()
    }

    fun removeFile(fileId: Long) {
        pageFileRepository.findById(fileId).ifPresent { f ->
            transaction {
                pageFileRepository.delete(f)
            }

            val dest = File(".${f.url()}").absoluteFile
            dest.parentFile?.listFiles()?.filter { it.name.startsWith("$fileId.") }?.forEach {
                it.delete()
            }
        }

        Caches.instance.reset()
    }

    fun scale(file: File): Boolean {
        val type = PageFileType.values().firstOrNull { t ->
            t.ext.any { e -> file.name.endsWith(".$e", ignoreCase = true) }
        } ?: return false

        if(!type.image) return false

        Regex("([0-9]+)\\.h([0-9]+)\\.").find(file.name)?.let { m ->
            val fileId = m.groupValues[1].toLong()
            val height = m.groupValues[2].toInt()

            if(scales.none { it.type == KcmsImageScaleType.HEIGHT && it.size == height }) return false

            val inputFile = File(file.parentFile.absolutePath + "/$fileId.${type.ext.first()}")
            if(!inputFile.exists()) return false

            if(resizeImageByHeight(inputFile, file, height, type.ext.first())) return true
        }

        Regex("([0-9]+)\\.w([0-9]+)\\.").find(file.name)?.let { m ->
            val fileId = m.groupValues[1].toLong()
            val width = m.groupValues[2].toInt()

            if(scales.none { it.type == KcmsImageScaleType.WIDTH && it.size == width }) return false

            val inputFile = File(file.parentFile.absolutePath + "/$fileId.${type.ext.first()}")
            if(!inputFile.exists()) return false

            if(resizeImageByWidth(inputFile, file, width, type.ext.first())) return true
        }

        return false
    }

    fun resizeImageByWidth(inputFile: File, outputFile: File, targetWidth: Int, format: String): Boolean = try {
        val originalImage = ImageIO.read(inputFile)

        // Вычисление новой высоты с сохранением пропорций
        val originalWidth = originalImage.width
        val originalHeight = originalImage.height
        val targetHeight = (targetWidth * originalHeight) / originalWidth

        if(targetWidth >= originalWidth || targetHeight >= originalHeight) {
            // create a symlink
            Files.createSymbolicLink(outputFile.canonicalFile.toPath(), inputFile.canonicalFile.toPath())
        } else {
            // Создание нового изображения с нужными размерами
            val tmp = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
            val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)

            // Отрисовка изображения
            val g2d = resizedImage.createGraphics()
            g2d.drawImage(tmp, 0, 0, null)
            g2d.dispose()

            ImageIO.write(resizedImage, format, outputFile)
        }
        true
    }catch(e: Exception) {
        log.error(e.message, e)
        false
    }

    fun resizeImageByHeight(inputFile: File, outputFile: File, targetHeight: Int, format: String): Boolean = try {
        val originalImage = ImageIO.read(inputFile)

        // Вычисление новой высоты с сохранением пропорций
        val originalWidth = originalImage.width
        val originalHeight = originalImage.height
        val targetWidth = (targetHeight * originalWidth) / originalHeight

        if(targetWidth >= originalWidth || targetHeight >= originalHeight) {
            // create a symlink
            Files.createSymbolicLink(outputFile.canonicalFile.toPath(), inputFile.canonicalFile.toPath())
        } else {
            // Создание нового изображения с нужными размерами
            val tmp = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
            val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)

            // Отрисовка изображения
            val g2d = resizedImage.createGraphics()
            g2d.drawImage(tmp, 0, 0, null)
            g2d.dispose()

            ImageIO.write(resizedImage, format, outputFile)
        }
        true
    }catch(e: Exception) {
        log.error(e.message, e)
        false
    }

    fun save(pageId: Long?, url: String) {
        val url = URL(url)

        val origName = url.file?.replace(Regex(".*/([^/]+)$"), "$1") ?: ""

        val type = PageFileType.values().firstOrNull { t ->
            t.ext.any { e -> origName.endsWith(".$e", ignoreCase = true) }
        } ?: return


        val tempFile = File("files/${UUID.randomUUID()}").absoluteFile
        tempFile.parentFile.mkdirs()
        tempFile.outputStream().use { out ->
            url.openStream().use { input ->
                StreamUtils.copy(input, out)
            }
        }

        val f = PageFile(
            id = pageFileRepository.nextId(),
            pageId = pageId,
            type = type,
            origName = origName,
            size = tempFile.length()
        )

        tempFile.renameTo(File(".${f.url()}").absoluteFile.also {
            it.parentFile.mkdirs()
        })

        pageFileRepository.save(f)

        Caches.instance.reset()

    }

    fun getPagesFiles(pageIds: List<Long>): Map<Long, List<PageFile>> {
        val result = HashMap<Long, List<PageFile>>()

        val missedIds = pageIds.filter { pageId ->
            val files = filesByPageCache.getIfPresent(pageId)
            if(files != null) result[pageId] = files
            files == null
        }

        if(missedIds.isNotEmpty()) {
            val pageFiles = pageFileRepository.findByPageIdIn(missedIds)
                .map { it.copy() }
                .sortedBy { it.order }
                .groupBy { it.pageId }

            missedIds.forEach { pageId ->
                val files = pageFiles[pageId] ?: emptyList()
                result[pageId] = files
                filesByPageCache.put(pageId, files)
            }
        }

        return result
    }

    @PostConstruct
    fun postConstruct() {
        instance = this
    }

    companion object {
        lateinit var instance: PageFilesService
    }
}

enum class KcmsImageScaleType {
    WIDTH, HEIGHT
}

interface KcmsImageScale {
    val size: Int
    val type: KcmsImageScaleType
}