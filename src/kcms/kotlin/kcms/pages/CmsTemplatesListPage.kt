package kcms.pages

import kcms.ui.cms.CommonCMSPage
import kcms.ui.cms.MenuModule
import kcms.widgets.WidgetContainer
import kcms.widgets.Widget

class CmsTemplatesListPage(
    val templates: List<PageTemplate>
) : CommonCMSPage(
    title = "Page Templates",
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
                                href(CmsTemplateRoute(t.id))
                                +t.id
                            }
                        }
                        TD {
                            +(t.widgets?.sumOf { widgetsCount(it) } ?: 0).toString()
                        }
                        TD {
                            +(t.widgets?.sumOf { sharedPropertiesCount(it) } ?: 0).toString()
                        }
                        TD {
                            +(t.widgets?.sumOf { pagePropertiesCount(it) } ?: 0).toString()
                        }
                    }
                }
            }
        }
    }

    private fun widgetsCount(w: Widget): Int {
        return if(w is WidgetContainer) {
            1 + (w.children?.sumOf { widgetsCount(it) } ?: 0)
        } else {
            1
        }
    }

    private fun sharedPropertiesCount(w: Widget): Int {
        return if(w is WidgetContainer) {
            w.properties.count { it.shared } + (w.children?.sumOf { sharedPropertiesCount(it) } ?: 0)
        } else {
            w.properties.count { it.shared }
        }
    }

    private fun pagePropertiesCount(w: Widget): Int {
        return if(w is WidgetContainer) {
            w.properties.count { !it.shared } + (w.children?.sumOf { pagePropertiesCount(it) } ?: 0)
        } else {
            w.properties.count { !it.shared }
        }
    }

}