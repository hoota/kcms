package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization
import kcms.widgets.SitePropertiesDescriptor

class KcmsSiteSettingsPage(
    val sitePropertiesDescriptors: List<SitePropertiesDescriptor>
) : CommonKcmsPage(
    title = KcmsInternationalization.instance.settings,
    module = MenuModule.SETTINGS
) {

    override fun pageBody() {
        TABLE("table") {
            THEAD {
                TR {
                    TH(i18n.category)
                }
            }

            TBODY {
                sitePropertiesDescriptors.forEach { d ->
                    TR {
                        TD {
                            A {
                                href(KcmsSitePropertiesRoute(bean = d.javaClass.simpleName))
                                +d.title
                            }
                        }
                    }
                }
            }
        }
    }
}