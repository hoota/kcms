package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.widgets.Widget
import kcms.widgets.WidgetContainer

class KcmsTemplatesListPage(
    val templates: List<PageTemplate>
) : CommonKcmsPage(
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
                                href(KcmsTemplateRoute(t.id))
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
            w.properties.count { it.globalScope } + (w.children?.sumOf { sharedPropertiesCount(it) } ?: 0)
        } else {
            w.properties.count { it.globalScope }
        }
    }

    private fun pagePropertiesCount(w: Widget): Int {
        return if(w is WidgetContainer) {
            w.properties.count { !it.globalScope } + (w.children?.sumOf { pagePropertiesCount(it) } ?: 0)
        } else {
            w.properties.count { !it.globalScope }
        }
    }

}