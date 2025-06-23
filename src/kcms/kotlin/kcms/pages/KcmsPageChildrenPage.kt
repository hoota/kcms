package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization

class KcmsPageChildrenPage(
    val p: Page,
    val children: List<Page>,
) : CommonKcmsPage(
    title = "${KcmsInternationalization.instance.page} #${p.id} // ${p.title}",
    module = MenuModule.PAGES,
    showTitleAsHeader = false
) {
    private fun drawTabs() {
        DIV("nav nav-tabs mt-1 mb-1") {
            A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.PROPERTIES))
                +i18n.properties
            }
            A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.FILES))
                +i18n.files
            }
            A("nav-item nav-link active show") {
                href("#")
                +i18n.children
            }
        }
    }

    override fun pageBody() {
        H3 {
            classes("mt-3 page-title")
            A {
                href(p.slug)
                +"${i18n.page} #${p.id}"
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
                        TH(i18n.title)
                        TH(i18n.template)
                        TH(i18n.published)
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
                                +p.templateId
                            }
                            TD {
                                classes(if(p.published) "text-success" else "text-danger")
                                +if(p.published) i18n.yes else i18n.no
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
                                    title(i18n.moveToTop)
                                    onClick("moveRowOnTop(this, true)")
                                    +"⤒"
                                }
                                SPAN("btn btn-link move-value-up") {
                                    style("border: none; padding: 0 0;")
                                    title(i18n.moveUp)
                                    onClick("moveRowUp(this, true)")
                                    +"↑"
                                }
                                SPAN("btn btn-link move-value-down") {
                                    style("border: none; padding: 0 0;")
                                    title(i18n.moveDown)
                                    onClick("moveRowDown(this, true)")
                                    +"↓"
                                }
                                SPAN("btn btn-link move-value-down") {
                                    style("border: none; padding: 0 0;")
                                    title(i18n.moveToTheBottom)
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