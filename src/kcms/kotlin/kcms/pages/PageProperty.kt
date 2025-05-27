package kcms.pages

import kcms.common.CrudRepository
import org.springframework.stereotype.Repository
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "page_property")
data class PageProperty(
    @EmbeddedId
    val id: PagePropertyId,
    val text: String? = null,
    val date: LocalDate? = null,
    val number: Long? = null,
) : Serializable

@Embeddable
data class PagePropertyId(
    val pageId: Long,
    val widgetId: String,
    val propertyId: String,
) : Serializable

@Repository
interface PagePropertyRepository : CrudRepository<PageProperty, PagePropertyId> {
    fun findByIdPageId(pageId: Long): List<PageProperty>
    fun findByIdPageIdIn(pageIds: Iterable<Long>): List<PageProperty>
}