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
    val rootProperties: Map<String, Map<String, PageProperty>>?,
    val pageProperties: Map<String, Map<String, PageProperty>>?,
    val pageFiles: List<PageFile>,
) : WidgetRenderContext {

    override fun getProperty(widgetId: String, propertyKey: String): PageProperty? =
        pageProperties?.get(widgetId)?.get(propertyKey) ?: rootProperties?.get(widgetId)?.get(propertyKey)

    override fun getProperty(widget: Widget, property: WidgetPropertyDescriptor): PageProperty? =
        pageProperties?.get(widget.id)?.get(property.key) ?: rootProperties?.get(widget.id)?.get(property.key)

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