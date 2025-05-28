package kcms.files

import com.google.common.cache.CacheBuilder
import kcms.common.Caching
import kcms.common.CommonService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

@Service
class PageFilesService(
    val pageFileRepository: PageFileRepository,
    val scales: List<CmsImageScale>,
) : CommonService(), Caching {

    private val filesByPageCache = CacheBuilder.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .softValues()
        .build<Long, List<PageFile>>()

    override fun resetCaches() {
        filesByPageCache.invalidateAll()
    }

    fun getPageFiles(pageId: Long): List<PageFile> = filesByPageCache.get(pageId) {
        pageFileRepository.findByPageId(pageId).map { it.copy() }
    }

    fun save(pageId: Long, file: MultipartFile) {
        val origName = file.originalFilename ?: ""

        val type = PageFileType.values().firstOrNull { t ->
            t.ext.any { e -> origName.endsWith(".$e", ignoreCase = true) }
        } ?: return

        val f = pageFileRepository.save(PageFile(
            id = pageFileRepository.nextId(),
            pageId = pageId,
            type = type,
            origName = origName
        ))

        val dest = File(".${f.url()}").absoluteFile
        dest.parentFile.mkdirs()
        file.transferTo(dest)

        filesByPageCache.invalidate(pageId)
    }

    fun removeFile(pageId: Long, fileId: Long) {
        pageFileRepository.findById(fileId).ifPresent { f ->
            transaction {
                pageFileRepository.delete(f)
            }

            val dest = File(".${f.url()}").absoluteFile
            dest.parentFile?.listFiles()?.filter { it.name.startsWith("$fileId.") }?.forEach {
                it.delete()
            }
        }

        filesByPageCache.invalidate(pageId)
    }

    fun scale(file: File): Boolean {
        val type = PageFileType.values().firstOrNull { t ->
            t.ext.any { e -> file.name.endsWith(".$e", ignoreCase = true) }
        } ?: return false

        if(!type.image) return false

        Regex("([0-9]+)\\.h([0-9]+)\\.").find(file.name)?.let { m ->
            val fileId = m.groupValues[1].toLong()
            val height = m.groupValues[2].toInt()

            if(scales.none { it.type == CmsImageScaleType.HEIGHT && it.size == height }) return false

            val inputFile = File(file.parentFile.absolutePath + "/$fileId.${type.ext.first()}")
            if(!inputFile.exists()) return false

            if(resizeImageByHeight(inputFile, file, height, type.ext.first())) return true
        }

        Regex("([0-9]+)\\.w([0-9]+)\\.").find(file.name)?.let { m ->
            val fileId = m.groupValues[1].toLong()
            val width = m.groupValues[2].toInt()

            if(scales.none { it.type == CmsImageScaleType.WIDTH && it.size == width }) return false

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

        // Создание нового изображения с нужными размерами
        val tmp = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
        val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)

        // Отрисовка изображения
        val g2d = resizedImage.createGraphics()
        g2d.drawImage(tmp, 0, 0, null)
        g2d.dispose()

        ImageIO.write(resizedImage, format, outputFile)
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

        // Создание нового изображения с нужными размерами
        val tmp = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
        val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)

        // Отрисовка изображения
        val g2d = resizedImage.createGraphics()
        g2d.drawImage(tmp, 0, 0, null)
        g2d.dispose()

        ImageIO.write(resizedImage, format, outputFile)
        true
    }catch(e: Exception) {
        log.error(e.message, e)
        false
    }

}

enum class CmsImageScaleType {
    WIDTH, HEIGHT
}

interface CmsImageScale {
    val size: Int
    val type: CmsImageScaleType
}