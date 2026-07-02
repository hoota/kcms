package kcms.ui.cms

import kcms.ui.KcmsGossRenderer
import kcms.ui.KcmsGossRendererView
import kcms.ui.cms.i18n.KcmsInternationalization
import kiss.gossr.spring.GetRoute
import java.util.*

abstract class CommonKcmsPage(
    val title: String? = null,
    val module: MenuModule? = null,
    val showTitleAsHeader: Boolean = true,
    val showMenu: Boolean = true,
    val mainContainerClass: String = "container-fluid"
) : KcmsGossRendererView() {

    open fun preTitle() {}
    abstract fun pageBody()

    fun pageBodyWrapper() {
        DIV("mb-4") {
            id(this.javaClass.simpleName)
            pageBody()
        }
    }

    open fun additionalHeaders() {}

    override fun draw() {
        noEscape("<!DOCTYPE html>")
        HTML {
            attr("lang", "en")
            HEAD {
                EL("meta") { attr("charset", "utf-8") }
                EL("meta") {
                    name("viewport")
                    attr("content", "width=device-width, initial-scale=1, shrink-to-fit=no")
                }

                LINK(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/bootstrap@4.4.1/dist/css/bootstrap.min.css")
                LINK(rel = "stylesheet", href = "https://ajax.googleapis.com/ajax/libs/jqueryui/1.13.2/jquery-ui.min.css")
                LINK(rel = "stylesheet", href = "https://ajax.googleapis.com/ajax/libs/jqueryui/1.13.2/jquery-ui.structure.min.css")

                STYLE("""
                    .size-80 { font-size: 80%;}
                    .pointer { cursor: pointer !important; }
                    .nobr { white-space: nowrap; }
                    .expandable { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                    .dropdown-item { cursor: pointer; }
                    .bg-selected { background-color: #f2fff2!important; }
                    .bg-gray { background-color: #eee!important; }
                    table .merged-row { border-top: 0; padding-top: 0; }
                    table.no-top-border thead tr:first-child th { border-top: none; }
                    tr:first-of-type span.move-value-up { display: none; }          
                    tr:last-of-type span.move-value-down { display: none; }          
                    
                    ::placeholder {
                        transition: opacity 0.2s;
                        font-style: italic;
                        __color: #28a745!important;
                    }
                    :hover::placeholder, :focus::placeholder { transition: opacity 0.2s; opacity: 0; }
                    @media (max-width: 1800px) {
                        .actions-dropright {
                            display: none;
                        }
                    }
                """)

                LINK { rel("apple-touch-icon"); attr("sizes", "180x180"); href("/apple-touch-icon.png") }
                LINK { rel("icon"); type("image/png"); attr("sizes", "32x32"); href("/favicon-32x32.png") }
                LINK { rel("icon"); type("image/png"); attr("sizes", "16x16"); href("/favicon-16x16.png") }
                LINK(rel = "manifest", href = "/site.webmanifest")

                SCRIPT(src = "https://ajax.googleapis.com/ajax/libs/jquery/3.6.1/jquery.min.js")
                SCRIPT(src = "https://ajax.googleapis.com/ajax/libs/jqueryui/1.13.2/jquery-ui.min.js")
                SCRIPT(src = "https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js")
                SCRIPT(src = "https://cdn.jsdelivr.net/npm/bootstrap@4.4.1/dist/js/bootstrap.min.js")
                SCRIPT(src = getResourceUrlWithVersion("/assets/kcms-script.js"))

                EL("title") {
                    +title
                }

                additionalHeaders()
            }
            BODY {
                if(showMenu) {
                    KcmsMenuBlock().draw(module)
                }

                DIV(mainContainerClass) {
                    preTitle()

                    if(title != null && showTitleAsHeader) {
                        H3 {
                            classes("mt-3 page-title")
                            +title
                        }
                    }

                    try {
                        pageBodyWrapper()
                    }catch(e: Exception) {
                        DIV("alert alert-danger") {
                            PRE {
                                +e.stackTraceToString()
                            }
                        }
                    }
                }

                drawScripts()

            }
        }
    }

    private fun drawScripts() {
        SCRIPT(code = """
$(document).ready($(function() { 
    $('[data-toggle="tooltip"]').tooltip() 
    $('.js-date').datepicker({dateFormat: "yy-mm-dd"});
}));
    """)
    }

    fun includeTrumbowyg() {
        LINK(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/trumbowyg@2.31.0/dist/ui/trumbowyg.min.css")
        SCRIPT(src = "https://cdn.jsdelivr.net/npm/trumbowyg@2.31.0/dist/trumbowyg.min.js")
        if(KcmsInternationalization.language != "en") {
            SCRIPT(src = "https://cdn.jsdelivr.net/npm/trumbowyg@2.31.0/dist/langs/${KcmsInternationalization.language}.min.js")
        }
    }

    internal data class Tab(
        val title: String,
        val selected: Boolean = false,
        val route: GetRoute? = null,
        val show: () -> Boolean = { true },
        val id: String = "tab${UUID.randomUUID().toString().replace("-", "")}",
        val content: () -> Any = { },
    )

    internal fun drawTabs(tabs: List<Tab>, maxItems: Int = Int.MAX_VALUE) {
        fun menuItem(classes: String, t: Tab) {
            A {
                classes(classes)
                if(t.selected) classes("active show")
                if(t.route != null) {
                    href(t.route)
                } else {
                    id("${t.id}-tab")
                    attr("data-toggle", "tab")
                    href("#${t.id}")
                    role("tab")
                    attr("aria-controls", t.id)
                    attr("aria-selected", "${t.selected}")
                }
                +t.title
            }
        }

        DIV("nav nav-tabs mt-1") {
            role("tablist")

            tabs.filter { it.selected || it.show() }.forEachIndexed { index, t ->
                if(index < maxItems || t.selected) {
                    menuItem("nav-item nav-link", t)
                }
            }

            tabs.filter { it.selected || it.show() }.forEachIndexed { index, t ->
                if(index == maxItems) {
                    LI("nav-item dropdown") {
                        A {
                            attr("data-toggle", "dropdown")
                            attr("role", "button")
                            classes("nav-link dropdown-toggle")
                            href("#")
                            +i18n.more
                        }
                        DIV("dropdown-menu") {
                            tabs.filter { it.selected || it.show() }.forEachIndexed { index, t ->
                                if(index >= maxItems && !t.selected) {
                                    menuItem("dropdown-item", t)
                                }
                            }
                        }
                    }
                }
            }
        }

        DIV("tab-content") {
            tabs.filter { it.selected || it.show() }.forEach { t ->
                DIV(if(t.selected) "tab-pane fade show active mt-4" else "tab-pane fade mt-4") {
                    id(t.id)
                    role("tabpanel")
                    attr("aria-labelledby", "${t.id}-tab")
                    if(t.route == null || t.selected) t.content()
                }

            }
        }
    }
}

interface WithOrdersRoute {
    val orders: MutableMap<Long, Int>
}

fun KcmsGossRenderer.orderChangeBlock(
    route: WithOrdersRoute,
    id: Long,
    order: Int,
    submitOnClick: Boolean
) {
    namePrefix(route::orders) {
        INPUT("order") {
            name(id.toString())
            type("hidden")
            value(order)
        }
    }
    SPAN("btn btn-link move-value-up") {
        style("border: none; padding: 0 0;")
        title(i18n.moveToTop)
        onClick("moveRowOnTop(this, $submitOnClick)")
        +"⤒"
    }
    SPAN("btn btn-link move-value-up") {
        style("border: none; padding: 0 0;")
        title(i18n.moveUp)
        onClick("moveRowUp(this, $submitOnClick)")
        +"↑"
    }
    SPAN("btn btn-link move-value-down") {
        style("border: none; padding: 0 0;")
        title(i18n.moveDown)
        onClick("moveRowDown(this, $submitOnClick)")
        +"↓"
    }
    SPAN("btn btn-link move-value-down") {
        style("border: none; padding: 0 0;")
        title(i18n.moveToTheBottom)
        onClick("moveRowToBottom(this, $submitOnClick)")
        +"⤓"
    }
}
