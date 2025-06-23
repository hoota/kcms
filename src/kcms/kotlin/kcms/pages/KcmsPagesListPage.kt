package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.PagedData
import kcms.ui.cms.Paginator
import kcms.ui.cms.i18n.KcmsInternationalization
import kiss.gossr.GossRendererTypedSelect

class KcmsPagesListPage(
    val route: KcmsPagesController.KcmsPagesListRoute,
    val templates: List<PageTemplate>,
    val parents: List<Page>,
    val pages: PagedData<Page>
) : CommonKcmsPage(
    title = KcmsInternationalization.instance.pages,
    module = MenuModule.PAGES
) {

    override fun preTitle() {
        A("btn btn-primary") {
            style("float: right;")
            href(KcmsPageNewRoute())
            +i18n.newPage
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
                    TH(i18n.title)
                    TH(i18n.template)
                    TH(i18n.published)
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
                                href(KcmsTemplateRoute(p.templateId))
                                +p.templateId
                            }
                        }
                        TD {
                            classes(if(p.published) "text-success" else "text-danger")
                            +if(p.published) i18n.yes else i18n.no
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
                    placeholder(i18n.searchQuery)
                    nameValueString(route::query)
                }
                DIV("input-group-append") {
                    DIV("input-group-text") {
                        val chId = CHECKBOX(route::searchContent, withId = true)
                        LABEL("ml-1 mb-0") {
                            forAttr(chId)
                            style("margin-top: -2px;")
                            +i18n.searchInContent
                        }
                    }
                    SELECT(route::templateId) {
                        classes("input-group-text")
                        style("background-color: white;")
                        OPTION("-- ${i18n.anyTemplate} --")
                        templates.forEach { t ->
                            OPTION(t.templateId)
                        }
                    }
                    SELECT(route::parentId) {
                        classes("input-group-text")
                        style("background-color: white;max-width: 200px")
                        OPTION("-- ${i18n.anyParent} --")
                        this.drawParentOptions(null, "")
                    }
                    SUBMIT("btn btn-outline-secondary", i18n.search)
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