package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization
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
    title = "${KcmsInternationalization.instance.template} // ${template.templateId}",
    module = MenuModule.TEMPLATES
) {

    override fun pageBody() {
        FORM(KcmsTemplateSaveRoute(templateId = template.templateId)) { route ->
            HIDDEN(route::templateId)

            KcmsPropertiesEditBlock(route, properties).drawWidgets(template.globalWidgets)

            SUBMIT("btn btn-primary", route::doSave, i18n.save)
        }
    }
}