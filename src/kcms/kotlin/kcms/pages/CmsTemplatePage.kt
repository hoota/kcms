package kcms.pages

import kcms.ui.cms.CommonCMSPage
import kcms.ui.cms.MenuModule
import kcms.widgets.WidgetContainer
import kcms.widgets.Widget
import kcms.widgets.WidgetPropertyType
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute

data class CmsTemplateRoute(
    val templateId: String
) : GetRoute

data class CmsTemplateSaveRoute(
    val templateId: String,
    val properties: MutableMap<String, MutableMap<String, String>> = HashMap(),
    val doSave: String? = null,
) : PostRoute

class CmsTemplatePage(
    val template: PageTemplate,
    val properties: Map<String, Map<String, PageProperty>>,
) : CommonCMSPage(
    title = "Page Template - ${template.id}",
    module = MenuModule.TEMPLATES
) {

    override fun pageBody() {
        FORM(CmsTemplateSaveRoute(templateId = template.id)) { route ->
            HIDDEN(route::templateId)
            drawSharedWidgets(route, template.widgets)
            SUBMIT("btn btn-primary", route::doSave, "Save")
        }
    }

    private fun hasSharedProperties(w: Widget): Boolean = w.properties.any { it.shared } ||
        (w is WidgetContainer && w.children?.any { hasSharedProperties(it) } ?: false)

    private fun drawSharedWidgets(route: CmsTemplateSaveRoute, widgets: List<Widget>?) {
        widgets?.filter { hasSharedProperties(it) }?.forEach { w ->
            B { +w.title }
            DIV("ml-4") {
                namePrefix(route::properties, w.id) {
                    CmsPropertiesEditBlock().draw(properties, w.id, w.properties.filter { it.shared })
                }

                if(w is WidgetContainer) drawSharedWidgets(route, w.children)
            }
        }
    }
}