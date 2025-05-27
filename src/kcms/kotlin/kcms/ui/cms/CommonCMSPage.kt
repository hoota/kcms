package kcms.ui.cms

import kcms.ui.KCMSGossRendererView
import kiss.gossr.spring.GetRoute
import java.util.*

abstract class CommonCMSPage(
    val title: String? = null,
    val module: MenuModule? = null,
    val showTitleAsHeader: Boolean = true,
    val showMenu: Boolean = true,
    val mainContainerClass: String = "container-fluid"
) : KCMSGossRendererView() {

    open fun preTitle() {}
    abstract fun pageBody()

    fun pageBodyWrapper() {
        DIV {
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
                    
                    ::placeholder {
                        transition: opacity 0.2s;
                        font-style: italic;
                        color: #28a745!important;
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

                EL("title") {
                    +title
                }

                additionalHeaders()
            }
            BODY {
                if(showMenu) {
                    CmsMenuBlock().draw(module)
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
                    
function openCommonModal(event, url) {
    event.stopPropagation();
    event.preventDefault();
    $(document).trigger('click')

    fetch(url).then(response => {
        if (response.redirected) {
            window.location.href = response.url;
        }
        if(response.ok) {
            response.text().then(html => {
                var placeholder = $("<div/>").append(html)
                $(document.body).append(placeholder);
                placeholder.find(".modal").first().modal('show').on("hidden.bs.modal", function() { placeholder.remove(); });
            }) 
        } else {
            window.alert("Http Request not OK\nStatus:" + response.status + "\nURL: " + response.url)
        }
    }).catch(function(err) {
        window.alert(err);
    });
    
    return false
}

function ajaxFormSubmit(form, event) {
    try {
        var data = new FormData(form)
        if(event?.submitter?.name) {
            data.append(event?.submitter?.name, event?.submitter?.value)
        }
        form = $(form)
        form.closest('.modal').modal('hide')
        $('.modal-backdrop').remove();
        var actionUrl = form.attr('action');
        var method = form.attr('method') || 'GET';
        var request = method.toLowerCase() === 'get' ? { method: method } : { method: method, body: data }
        fetch(actionUrl, request).then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            }
            if(response.ok) response.text().then(html => {
                try {
                    var el = $(html);
                    if(el.hasClass('modal')) {
                        var placeholder = $("<div/>").append(el)
                        $(document.body).append(placeholder);
                        placeholder.find(".modal").first().modal('show').on("hidden.bs.modal", function() { placeholder.remove(); }); 
                    } else {
                        var elId = el.attr('id')
                        $(elId ? ('#' + elId) : form).replaceWith(el)
                    }
                } catch(e) {
                    window.alert("Response is not valid jQuery selector");
                }
            });
            form.find('.js-ajaxFormSubmit-disable').attr('disabled', null).removeClass('js-ajaxFormSubmit-disable');
        }).catch(function(err) {
            window.alert(err);
        });

        form.find('button, input[type=submit]:not(:disabled)').attr('disabled', 'disabled').addClass("js-ajaxFormSubmit-disable")
    } catch(e){
        console.error(e)
    }
    return false
}

    """)

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
                            +"More..."
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
