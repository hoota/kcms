package kcms.pages

import kcms.common.EntityWithLongId
import kcms.common.LongIdCrudRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "page")
data class Page(
    @Id
    override val id: Long = 0,
    var slug: String,
    var title: String,
    var template: String,
    var parentId: Long? = null,
) : EntityWithLongId {

    companion object {
        const val GENERATOR_NAME = "page_id_seq"
    }
}

@Repository
interface PagesRepository : LongIdCrudRepository<Page> {
    fun findBySlug(slug: String): Page?

    @Query("""SELECT nextval('${Page.GENERATOR_NAME}')""", nativeQuery = true)
    fun nextPageId(): Long
}