package kcms.pages

import kcms.enums.EnumValue
import kcms.enums.EnumValueService
import kcms.files.PageFile
import kcms.widgets.PagePropertyDescriptor
import kcms.widgets.Widget
import kcms.widgets.WidgetRenderContext
import org.springframework.web.bind.ServletRequestDataBinder
import org.springframework.web.servlet.View
import java.math.BigDecimal
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest

data class PageTemplateRenderContext(
    val page: Page,
    val siteProperties: Map<String, SiteProperty>? = null,
    val pageProperties: Map<String, PageProperty>? = null,
    val pageFiles: List<PageFile> = emptyList(),
) : WidgetRenderContext {

    override fun getProperty(propertyKey: String): KcmsProperty? =
        pageProperties?.get(propertyKey) ?: siteProperties?.get(propertyKey)

}

fun <T> HttpServletRequest.bindParams(params: T): T = try {
    val binder = ServletRequestDataBinder(params)
    binder.bind(this)
    params
}catch(e: Exception) {
    params
}

interface PageTemplate {
    val templateId: String
    val widgets: List<Widget>? get() = null

    fun view(request: HttpServletRequest, context: PageTemplateRenderContext): View
}

interface CouldBeParentPageTemplate : PageTemplate

interface WithPageTemplateRenderContext {
    val pageContext: PageTemplateRenderContext

    fun PagePropertyDescriptor.AsText.value(): String? = pageContext.getProperty(this)?.text

    fun PagePropertyDescriptor.AsBool.value(): Boolean = pageContext.getProperty(this)?.text == "true"

    fun PagePropertyDescriptor.AsDate.value(): LocalDate? = pageContext.getProperty(this)?.date

    fun PagePropertyDescriptor.asNumber(): BigDecimal? = pageContext.getProperty(this)?.number

    fun PagePropertyDescriptor.AsLong.value(): Long? = pageContext.getProperty(this)?.number?.toLong()

    fun PagePropertyDescriptor.AsInt.value(): Int? = pageContext.getProperty(this)?.number?.toInt()

    fun PagePropertyDescriptor.AsList.value(): List<String>? = pageContext.getProperty(this)?.asList

    fun PagePropertyDescriptor.AsMap.value(): Map<Long, String>? = pageContext.getProperty(this)?.asMap

    fun PagePropertyDescriptor.AsEnum.value(): EnumValue? {
        return pageContext.getProperty(this)?.number?.toLong()?.let { id ->
            EnumValueService.instance.getEnumValues(this.category).firstOrNull { it.id == id }
        }
    }

}

