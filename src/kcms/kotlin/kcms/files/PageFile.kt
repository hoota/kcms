package kcms.files

import kcms.common.EntityWithLongId
import kcms.common.LongIdCrudRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "kcms_file")
data class PageFile(
    @Id
    override val id: Long = 0,
    val pageId: Long?,
    @Enumerated(EnumType.STRING)
    val type: PageFileType,
    val origName: String,
    val size: Long,
    val symlink: Long? = null,
    @Column(name = "ord")
    var order: Int = id.toInt()
) : EntityWithLongId {

    fun url(): String = (symlink ?: id).let { id -> "/files/${id / FILES_PER_FOLDER}/$id.${type.ext.first()}" }
    fun urlWithHeight(h: Int): String = (symlink ?: id).let { id -> "/files/${id / FILES_PER_FOLDER}/$id.h$h.${type.ext.first()}" }
    fun urlWithWidth(w: Int): String = (symlink ?: id).let { id -> "/files/${id / FILES_PER_FOLDER}/$id.w$w.${type.ext.first()}" }

    companion object {
        const val GENERATOR_NAME = "file_id_seq"
        const val FILES_PER_FOLDER = 200
    }
}

enum class PageFileType(
    val image: Boolean = false,
    val video: Boolean = false,
    val file: Boolean = false,
    val ext: Array<String>,
) {
    JPG(image = true, ext = arrayOf("jpg", "jpeg")),
    PNG(image = true, ext = arrayOf("png")),
    MP4(video = true, ext = arrayOf("mp4")),
    PDF(file = true, ext = arrayOf("pdf")),
}

@Repository
interface PageFileRepository : LongIdCrudRepository<PageFile> {
    fun findByPageId(pageId: Long?): List<PageFile>
    fun findByPageIdIn(pageIds: Iterable<Long>): List<PageFile>

    @Query("""SELECT nextval('${PageFile.GENERATOR_NAME}')""", nativeQuery = true)
    fun nextId(): Long
}