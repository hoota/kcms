package kcms.pages

import kcms.files.PageFile
import kcms.widgets.Widget
import kcms.widgets.WidgetRenderContext
import org.springframework.web.servlet.View

data class PageTemplateRenderContext(
    val page: Page,
    val rootProperties: Map<String, Map<String, PageProperty>>?,
    val pageProperties: Map<String, Map<String, PageProperty>>?,
    val pageFiles: List<PageFile>,
) : WidgetRenderContext {

    override fun getProperty(widgetId: String, propertyKey: String): PageProperty? =
        pageProperties?.get(widgetId)?.get(propertyKey) ?: rootProperties?.get(widgetId)?.get(propertyKey)
}

interface PageTemplate {
    val id: String
    val widgets: List<Widget>?

    fun view(context: PageTemplateRenderContext): View
}

interface CouldBeParentPageTemplate : PageTemplate