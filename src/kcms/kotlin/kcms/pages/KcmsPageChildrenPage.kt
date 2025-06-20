package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule

class KcmsPageChildrenPage(
    val p: Page,
    val children: List<Page>,
) : CommonKcmsPage(
    title = "Page #${p.id} // ${p.title}",
    module = MenuModule.PAGES,
    showTitleAsHeader = false
) {
    private fun drawTabs() {
        DIV("nav nav-tabs mt-1 mb-1") {
            A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.PROPERTIES))
                +"Properties"
            }
            A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.FILES))
                +"Files"
            }
            A("nav-item nav-link active show") {
                href("#")
                +"Children"
            }
        }
    }

    override fun pageBody() {
        H3 {
            classes("mt-3 page-title")
            A {
                href(p.slug)
                +"Page #${p.id}"
            }
            +" // ${p.title}"
        }

        drawTabs()

        STYLE("""
            tr:first-of-type span.move-value-up { display: none; }          
            tr:last-of-type span.move-value-down { display: none; }          
        """)

        FORM(KcmsPagesController.KcmsPagesOrderSaveRoute()) { route ->
            ajaxForm()
            TABLE("table no-top-border") {
                THEAD {
                    TR {
                        TH("ID")
                        TH("Slug")
                        TH("Title")
                        TH("Template")
                        TH("Published")
                        TH("")
                    }
                }
                TBODY {
                    children.sortedBy { it.order }.forEach { p ->
                        val tab = if(PageTemplatesService.instance.getTemplate(p) is CouldBeParentPageTemplate) KcmsPageTabs.CHILDREN else KcmsPageTabs.PROPERTIES
                        TR {
                            TD {
                                A {
                                    href(p.slug)
                                    +p.id.toString()
                                }
                            }
                            TD {
                                A {
                                    href(KcmsPageRoute(p.id, tab))
                                    +p.slug
                                }
                            }
                            TD {
                                A {
                                    href(KcmsPageRoute(p.id, tab))
                                    +p.title
                                }
                            }
                            TD {
                                +p.template
                            }
                            TD {
                                classes(if(p.published) "text-success" else "text-danger")
                                +if(p.published) "yes" else "no"
                            }
                            TD {
                                namePrefix(route::orders) {
                                    INPUT("order") {
                                        name(p.id.toString())
                                        type("hidden")
                                        value(p.order)
                                    }
                                }
                                SPAN("btn btn-link move-value-up") {
                                    style("border: none; padding: 0 0;")
                                    title("Move on top")
                                    onClick("moveRowOnTop(this, true)")
                                    +"⤒"
                                }
                                SPAN("btn btn-link move-value-up") {
                                    style("border: none; padding: 0 0;")
                                    title("Move up")
                                    onClick("moveRowUp(this, true)")
                                    +"↑"
                                }
                                SPAN("btn btn-link move-value-down") {
                                    style("border: none; padding: 0 0;")
                                    title("Move down")
                                    onClick("moveRowDown(this, true)")
                                    +"↓"
                                }
                                SPAN("btn btn-link move-value-down") {
                                    style("border: none; padding: 0 0;")
                                    title("Move to the bottom")
                                    onClick("moveRowToBottom(this, true)")
                                    +"⤓"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}