package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.PagedData
import kcms.ui.cms.Paginator
import kiss.gossr.GossRendererTypedSelect

class KcmsPagesListPage(
    val route: KcmsPagesController.KcmsPagesListRoute,
    val templates: List<PageTemplate>,
    val parents: List<Page>,
    val pages: PagedData<Page>
) : CommonKcmsPage(
    title = "Pages",
    module = MenuModule.PAGES
) {

    override fun preTitle() {
        A("btn btn-primary") {
            style("float: right;")
            href(KcmsPageNewRoute())
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
                    TH("Published")
                }
            }
            TBODY {
                pages.data.forEach { p ->
                    TR {
                        TD {
                            A {
                                href(p.slug)
                                +p.id.toString()
                            }
                        }
                        TD {
                            A {
                                href(KcmsPageRoute(p.id))
                                +p.slug
                            }
                        }
                        TD {
                            A {
                                href(KcmsPageRoute(p.id))
                                +p.title
                            }
                        }
                        TD {
                            A {
                                href(KcmsTemplateRoute(p.template))
                                +p.template
                            }
                        }
                        TD {
                            classes(if(p.published) "text-success" else "text-danger")
                            +if(p.published) "yes" else "no"
                        }
                    }
                }
            }
        }
    }

    private fun drawForm() {
        FORM(route) {
            DIV("input-group pl-0 mb-3 mt-2 col-12 col-md-9") {
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
                    SELECT(route::templateId) {
                        classes("input-group-text")
                        style("background-color: white;")
                        OPTION("-- any template --")
                        templates.forEach { t ->
                            OPTION(t.id)
                        }
                    }
                    SELECT(route::parentId) {
                        classes("input-group-text")
                        style("background-color: white;max-width: 200px")
                        OPTION("-- any parent --")
                        this.drawParentOptions(null, "")
                    }
                    SUBMIT("btn btn-outline-secondary", "Search")
                }
            }
        }
    }

    private fun GossRendererTypedSelect<Long>.drawParentOptions(parentId: Long?, prefix: String) {
        parents.filter { it.parentId == parentId }.forEach { p ->
            OPTION(p.id, "$prefix${p.title}")
            drawParentOptions(p.id, "$prefix- ")
        }
    }
}