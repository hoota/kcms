package kcms.pages

import com.fasterxml.jackson.annotation.JsonIgnore
import kcms.common.EntityWithLongId
import kcms.common.LongIdCrudRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "kcms_page")
data class Page(
    @Id
    override val id: Long = 0,
    var slug: String,
    var title: String,
    var template: String,
    var parentId: Long? = null,
    var published: Boolean = false,
    @Column(name = "ord")
    var order: Int = id.toInt()
) : EntityWithLongId {

    @get:JsonIgnore
    val parent: Page? get() = parentId?.let {
        PageTemplatesService.instance.getPage(it)
    }

    companion object {
        const val GENERATOR_NAME = "page_id_seq"
    }
}

@Repository
interface PagesRepository : LongIdCrudRepository<Page> {
    fun findBySlug(slug: String): Page?
    fun findByParentId(parentId: Long): List<Page>
    fun findByParentIdInOrderByOrder(parentIds: Iterable<Long>): List<Page>
    fun findByTemplateIn(templates: Iterable<String>): List<Page>

    @Query("""
        SELECT p.* FROM kcms_page p WHERE p.id IN (
            SELECT c.parent_id FROM kcms_page c WHERE c.parent_id IS NOT NULL
        )
    """, nativeQuery = true)
    fun findParents(): List<Page>

    @Query("""SELECT nextval('${Page.GENERATOR_NAME}')""", nativeQuery = true)
    fun nextPageId(): Long
}