package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.widgets.Widget
import kcms.widgets.WidgetContainer
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute

data class KcmsTemplateRoute(
    val templateId: String
) : GetRoute

data class KcmsTemplateSaveRoute(
    val templateId: String,
    override val properties: MutableMap<String, String> = HashMap(),
    override val listProperties: MutableMap<String, MutableList<String>> = HashMap(),
    override val enumMapProperties: MutableMap<String, String> = HashMap(),
    val doSave: String? = null,
) : PostRoute, WidgetPropertiesSaveRoute

class KcmsTemplatePage(
    val template: PageTemplate,
    val properties: Map<String, PageProperty>,
) : CommonKcmsPage(
    title = "Page Template - ${template.id}",
    module = MenuModule.TEMPLATES
) {

    override fun pageBody() {
        FORM(KcmsTemplateSaveRoute(templateId = template.id)) { route ->
            HIDDEN(route::templateId)
            drawSharedWidgets(route, template.widgets)
            SUBMIT("btn btn-primary", route::doSave, "Save")
        }
    }

    private fun hasSharedProperties(w: Widget): Boolean = w.properties.any { it.globalScope } ||
        (w is WidgetContainer && w.children?.any { hasSharedProperties(it) } ?: false)

    private fun drawSharedWidgets(route: KcmsTemplateSaveRoute, widgets: List<Widget>?): Unit = namePrefix(route::properties, reset = true) {
        val kcmsPropertiesEditBlock = KcmsPropertiesEditBlock(route, properties)

        widgets?.filter { hasSharedProperties(it) }?.forEach { w ->
            B { +w.title }
            DIV("ml-4") {
                kcmsPropertiesEditBlock.draw(w.properties.filter { it.globalScope })

                if(w is WidgetContainer) drawSharedWidgets(route, w.children)
            }
        }
    }
}