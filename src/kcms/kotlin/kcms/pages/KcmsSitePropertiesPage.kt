package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.widgets.SitePropertiesDescriptor
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute

data class KcmsSitePropertiesRoute(
    val bean: String
) : GetRoute

data class KcmsSitePropertiesSaveRoute(
    override val properties: MutableMap<String, String> = HashMap(),
    override val listProperties: MutableMap<String, MutableList<String>> = HashMap(),
    override val enumMapProperties: MutableMap<String, String> = HashMap(),
    val doSave: String? = null,
) : PostRoute, WidgetPropertiesSaveRoute

class KcmsSitePropertiesPage(
    val descriptor: SitePropertiesDescriptor,
    val properties: Map<String, SiteProperty>,
) : CommonKcmsPage(
    title = descriptor.title,
    module = MenuModule.SETTINGS
) {

    override fun pageBody() {
        includeTrumbowyg()

        FORM(KcmsSitePropertiesSaveRoute()) { route ->
            KcmsPropertiesEditBlock(route, properties).drawWidgets(descriptor.widgets)

            SUBMIT("btn btn-primary", route::doSave, i18n.save)
        }
    }
}