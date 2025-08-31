package kcms.pages

import kcms.common.CrudRepository
import kcms.ui.KcmsGossRenderer
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "kcms_page_property")
data class PageProperty(
    @EmbeddedId
    val id: PagePropertyId,
    override var text: String? = null,
    override var date: LocalDate? = null,
    override var number: BigDecimal? = null,
) : KcmsProperty {

    override val key: String
        get() = id.propertyId
}


@Embeddable
data class PagePropertyId(
    val pageId: Long,
    val propertyId: String,
) : Serializable

@Repository
interface PagePropertyRepository : CrudRepository<PageProperty, PagePropertyId> {
    fun findByIdPageId(pageId: Long): List<PageProperty>
    fun findByIdPageIdIn(pageIds: Iterable<Long>): List<PageProperty>

    @Modifying
    fun deleteByIdPageId(pageId: Long): Int
}