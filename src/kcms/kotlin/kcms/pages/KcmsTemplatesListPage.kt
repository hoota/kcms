package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization
import kcms.widgets.Widget

class KcmsTemplatesListPage(
    val templates: List<PageTemplate>
) : CommonKcmsPage(
    title = KcmsInternationalization.instance.templates,
    module = MenuModule.TEMPLATES
) {

    override fun pageBody() {
        TABLE("table") {
            THEAD {
                TR {
                    TH("Template ID")
                    TH("Widgets")
                    TH("Shared Properties")
                    TH("Page Properties")
                }
            }

            TBODY {
                templates.forEach { t ->
                    TR {
                        TD {
                            A {
                                href(KcmsTemplateRoute(t.templateId))
                                +t.templateId
                            }
                        }
                        TD {
                            +(t.widgets?.size ?: 0).toString()
                        }
                        TD {
                            +(t.globalWidgets?.sumOf { propertiesCount(it) } ?: 0).toString()
                        }
                        TD {
                            +(t.widgets?.sumOf { propertiesCount(it) } ?: 0).toString()
                        }
                    }
                }
            }
        }
    }

    fun propertiesCount(w: Widget): Int = w.sumOf { c ->
        c.sumOf { it.pds.size }
    }
}