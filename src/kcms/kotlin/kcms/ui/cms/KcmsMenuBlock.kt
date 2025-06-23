package kcms.ui.cms

import kcms.enums.EnumValueService
import kcms.enums.KcmsEnumsCategoriesRoute
import kcms.files.KcmsFilesController
import kcms.jobs.JobsRunnerService
import kcms.jobs.KcmsJobsController
import kcms.pages.KcmsPagesController
import kcms.ui.KcmsGossRenderer
import kcms.ui.cms.htmlparser.KcmsHtmlParseRoute

enum class MenuModule {
    PAGES, JOBS, TEMPLATES, ENUMS, FILES;
}

class KcmsMenuBlock : KcmsGossRenderer() {

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
                href(KcmsController.KcmsRoute())
                IMG(getResourceUrlWithVersion("/kcms/logo.png"), height = 40)
            }
            DIV("collapse navbar-collapse") {
                id("navbarSupportedContent")
                EL("UL") {
                    classes("navbar-nav mr-auto")

                    KcmsDashboardComponents.instance.menuOptions.forEach { o ->
                        val dd = o.dropdown()
                        LI("nav-item dropdown") {
                            A {
                                href("#")
                                classes("nav-link dropdown-toggle")
                                attr("data-toggle", "dropdown")
                                attr("role", "button")
                                +dd.first
                            }
                            DIV("dropdown-menu") {
                                attr("aria-labelledby", "navbarDropdown")
                                dd.second.forEach { (label, route) ->
                                    A("dropdown-item") {
                                        href(route)
                                        +label
                                    }
                                }
                            }
                        }
                    }

                    LI {
                        classes("nav-item")
                        if(module == MenuModule.PAGES) classes("active")

                        A {
                            classes("nav-link")
                            href(KcmsPagesController.KcmsPagesListRoute())
                            +i18n.pages
                        }
                    }
                    LI {
                        classes("nav-item")
                        if(module == MenuModule.TEMPLATES) classes("active")

                        A {
                            classes("nav-link")
                            href(KcmsPagesController.KcmsTemplatesListRoute())
                            +i18n.templates
                        }
                    }
                    LI {
                        classes("nav-item")
                        if(module == MenuModule.FILES) classes("active")

                        A {
                            classes("nav-link")
                            href(KcmsFilesController.KcmsFilesRoute())
                            +i18n.files
                        }
                    }
                    if(JobsRunnerService.instance.jobs.isNotEmpty()) LI {
                        classes("nav-item")
                        if(module == MenuModule.JOBS) classes("active")

                        A {
                            classes("nav-link")
                            href(KcmsJobsController.AdminJobsListRoute())
                            +i18n.backgroundJobs
                        }
                    }
                    if(EnumValueService.instance.categories.isNotEmpty()) LI {
                        classes("nav-item")
                        if(module == MenuModule.ENUMS) classes("active")

                        A {
                            classes("nav-link")
                            href(KcmsEnumsCategoriesRoute())
                            +i18n.enums
                        }
                    }
                    if(System.getProperty("dev-mode") == "true") LI {
                        classes("nav-item")

                        A {
                            classes("nav-link")
                            href(KcmsHtmlParseRoute())
                            +"Html2Code"
                        }
                    }
                }
            }
        }
    }
}