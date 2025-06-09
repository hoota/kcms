package kcms.pages

import kcms.common.CrudRepository
import kcms.ui.KcmsGossRenderer
import org.springframework.stereotype.Repository
import java.io.Serializable
import java.math.BigDecimal
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
    var text: String? = null,
    var date: LocalDate? = null,
    var number: BigDecimal? = null,
) : Serializable

var PageProperty.asList: List<String>?
    get() = try {
        KcmsGossRenderer.objectMapper.readValue(text, PagePropertyStringListValue::class.java)
    }catch(e: Exception) {
        null
    }
    set(v) {
        text = KcmsGossRenderer.objectMapper.writeValueAsString(v)
    }

var PageProperty.asMap: Map<Long, String>?
    get() = try {
        KcmsGossRenderer.objectMapper.readValue(text, PagePropertyMapValue::class.java)
    }catch(e: Exception) {
        null
    }
    set(v) {
        text = KcmsGossRenderer.objectMapper.writeValueAsString(v)
    }

class PagePropertyStringListValue : ArrayList<String>()

class PagePropertyMapValue : LinkedHashMap<Long, String>()

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