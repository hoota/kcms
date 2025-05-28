package kcms.pages

import kcms.ui.cms.CommonCMSPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.PagedData
import kcms.ui.cms.Paginator

class CMSPagesListPage(
    val route: CMSPagesController.CmsPagesListRoute,
    val pages: PagedData<Page>
) : CommonCMSPage(
    title = "Pages",
    module = MenuModule.PAGES
) {

    override fun preTitle() {
        A("btn btn-primary") {
            style("float: right;")
            href(CMSPagesController.CmsPageRoute(-1))
            +"New Page"
        }
    }

    override fun pageBody() {
        drawForm()
        drawTable()

        Paginator().draw(pages) {
            route.copy(page = it)
        }
    }

    private fun drawTable() {
        TABLE("table") {
            THEAD {
                TR {
                    TH("ID")
                    TH("Slug")
                    TH("Title")
                    TH("Template")
                }
            }
            TBODY {
                pages.data.forEach { p ->
                    TR {
                        TD {
                            A {
                                href(CMSPagesController.CmsPageRoute(p.id))
                                +p.id.toString()
                            }
                        }
                        TD {
                            A {
                                href(CMSPagesController.CmsPageRoute(p.id))
                                +p.slug
                            }
                        }
                        TD {
                            A {
                                href(CMSPagesController.CmsPageRoute(p.id))
                                +p.title
                            }
                        }
                        TD {
                            A {
                                href(CmsTemplateRoute(p.template))
                                +p.template
                            }
                        }
                    }
                }
            }
        }
    }

    private fun drawForm() {
        FORM(route) {
            DIV("input-group pl-0 mb-3 mt-2 col-12 col-md-6") {
                INPUT("form-control") {
                    placeholder("Search query")
                    nameValueString(route::query)
                }
                DIV("input-group-append") {
                    DIV("input-group-text") {
                        val chId = CHECKBOX(route::searchContent, withId = true)
                        LABEL("ml-1 mb-0") {
                            forAttr(chId)
                            style("margin-top: -2px;")
                            +" search in content"
                        }
                    }
                    SUBMIT("btn btn-outline-secondary", "Search")
                }
            }
        }
    }
}