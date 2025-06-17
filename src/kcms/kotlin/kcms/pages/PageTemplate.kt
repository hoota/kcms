package kcms.pages

import kcms.files.PageFile
import kcms.widgets.Widget
import kcms.widgets.WidgetPropertyDescriptor
import kcms.widgets.WidgetRenderContext
import org.springframework.web.bind.ServletRequestDataBinder
import org.springframework.web.servlet.View
import javax.servlet.http.HttpServletRequest

data class PageTemplateRenderContext(
    val request: HttpServletRequest,
    val page: Page,
    val rootProperties: Map<String, PageProperty>? = null,
    val pageProperties: Map<String, PageProperty>? = null,
    val pageFiles: List<PageFile> = emptyList(),
) : WidgetRenderContext {

    override fun getProperty(propertyKey: String): PageProperty? =
        pageProperties?.get(propertyKey) ?: rootProperties?.get(propertyKey)

    fun <T> bindParams(params: T): T = try {
        val binder = ServletRequestDataBinder(params)
        binder.bind(request)
        params
    }catch(e: Exception) {
        params
    }

}

interface PageTemplate {
    val id: String
    val widgets: List<Widget>?

    fun view(context: PageTemplateRenderContext): View
}

interface CouldBeParentPageTemplate : PageTemplate