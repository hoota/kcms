package kcms.ui.cms

import kcms.enums.CmsEnumsCategoriesRoute
import kcms.enums.EnumValueService
import kcms.jobs.JobsController
import kcms.pages.CMSPagesController
import kcms.ui.KCMSGossRenderer
import kcms.ui.cms.htmlparser.CmsHtmlParseRoute

enum class MenuModule {
    PAGES, JOBS, TEMPLATES, ENUMS;
}

class CmsMenuBlock : KCMSGossRenderer() {

    fun draw(module: MenuModule?) {
        EL("NAV") {
            classes("navbar navbar-expand-lg navbar-light bg-light")
            BUTTON {
                classes("navbar-toggler")
                type("button")
                attr("data-toggle", "collapse")
                attr("data-target", "#navbarSupportedContent")
                attr("aria-controls", "navbarSupportedContent")
                attr("aria-expanded", "false")
                attr("aria-label", "Toggle navigation")
                SPAN {
                    classes("navbar-toggler-icon")
                }
            }
            A {
                classes("navbar-brand p-0")
                href(CMSController.CmsRoute())
                IMG("/cms-logo.png", height = 40)
            }
            DIV("collapse navbar-collapse") {
                id("navbarSupportedContent")
                EL("UL") {
                    classes("navbar-nav mr-auto")
                    LI {
                        classes("nav-item")
                        if(module == MenuModule.PAGES) classes("active")

                        A {
                            classes("nav-link")
                            href(CMSPagesController.CmsPagesListRoute())
                            +"Pages"
                        }
                    }
                    LI {
                        classes("nav-item")
                        if(module == MenuModule.TEMPLATES) classes("active")

                        A {
                            classes("nav-link")
                            href(CMSPagesController.CmsTemplatesListRoute())
                            +"Templates"
                        }
                    }
                    LI {
                        classes("nav-item")
                        if(module == MenuModule.JOBS) classes("active")

                        A {
                            classes("nav-link")
                            href(JobsController.AdminJobsListRoute())
                            +"Jobs"
                        }
                    }
                    if(EnumValueService.instance.categories.isNotEmpty()) LI {
                        classes("nav-item")
                        if(module == MenuModule.ENUMS) classes("active")

                        A {
                            classes("nav-link")
                            href(CmsEnumsCategoriesRoute())
                            +"Enums"
                        }
                    }
                    LI {
                        classes("nav-item")
                        if(module == MenuModule.JOBS) classes("active")

                        A {
                            classes("nav-link")
                            href(CmsHtmlParseRoute())
                            +"Html2Code"
                        }
                    }
                }
            }
        }
    }
}