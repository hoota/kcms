package kcms.pages

import kcms.ui.cms.CommonCMSPage
import kcms.ui.cms.MenuModule

class CMSPagesListPage(
    val pages: List<Page>
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
                pages.forEach { p ->
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
}