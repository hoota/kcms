package kcms.pages

import kcms.files.KcmsFilesListBlock
import kcms.files.KcmsImageScale
import kcms.files.KcmsImageScaleType
import kcms.files.PageFile
import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.widgets.Widget
import kcms.widgets.WidgetContainer
import kiss.gossr.GossRendererTypedSelect
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute
import org.springframework.stereotype.Component

enum class KcmsPageTabs {
    PROPERTIES, FILES, CHILDREN
}

data class KcmsPageRoute(val id: Long, val tab: KcmsPageTabs? = null) : GetRoute

data class KcmsPageNewRoute(
    val templateId: String? = null,
    val parentId: Long? = null,
    val slug: String? = null,
) : GetRoute

interface WidgetPropertiesSaveRoute {
    val properties: MutableMap<String, String>
    val listProperties: MutableMap<String, MutableList<String>>
    val enumMapProperties: MutableMap<String, String>
}

data class KcmsPageSaveRoute(
    val pageId: Long,
    val pageSlug: String,
    val pageTitle: String,
    val pageTemplate: String,
    val parentId: Long?,
    val published: Boolean?,
    override val properties: MutableMap<String, String> = HashMap(),
    override val listProperties: MutableMap<String, MutableList<String>> = HashMap(),
    override val enumMapProperties: MutableMap<String, String> = HashMap(),
    val doSave: String? = null,
    val doSaveAndContinue: String? = null,
    val doRemove: String? = null,
) : PostRoute, WidgetPropertiesSaveRoute

class KcmsPagePage(
    val templates: List<PageTemplate>,
    val parents: List<Page>,
    val p: Page,
    val template: PageTemplate?,
    val properties: Map<String, PageProperty>,
    val noChildren: Boolean = true
) : CommonKcmsPage(
    title = if(p.id >= 0) "Page #${p.id} // ${p.title}" else "New Page",
    module = MenuModule.PAGES,
    showTitleAsHeader = false
) {

    private fun drawTabs() {
        DIV("nav nav-tabs mt-1 mb-1") {
            A("nav-item nav-link active show") {
                href("#")
                +"Properties"
            }
            A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.FILES))
                +"Files"
            }
            if(!noChildren) A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.CHILDREN))
                +"Children"
            }
        }
    }

    override fun pageBody() {

        if(p.id >= 0) {
            H3 {
                classes("mt-3 page-title")
                A {
                    href(p.slug)
                    +"Page #${p.id}"
                }
                +" // ${p.title}"
            }

            drawTabs()
        } else {
            H3 {
                classes("mt-3 page-title")
                +"New Page"
            }
        }

        FORM(KcmsPageSaveRoute(
            pageId = p.id,
            pageSlug = p.slug,
            pageTitle = p.title,
            pageTemplate = p.template,
            parentId = p.parentId,
            published = p.published,
        )) { route ->
            HIDDEN(route::pageId)

            DIV("row") {
                DIV("col-12 form-group col-md") {
                    LABEL {
                        +"Page Slug (URI)"
                    }
                    INPUT("form-control") {
                        nameValueString(route::pageSlug)
                    }
                }
                DIV("col-12 form-group col-md") {
                    LABEL {
                        +"Parent"
                    }
                    SELECT(route::parentId) {
                        classes("form-control")
                        OPTION("-- no parent --")
                        this.drawParentOptions(null, "")
                    }
                }
                DIV("col-12 form-group col-md") {
                    LABEL {
                        +"Page Template"
                    }
                    SELECT(route::pageTemplate) {
                        classes("form-control")
                        templates.forEach { t ->
                            OPTION(t.id)
                        }
                    }
                }
                DIV("col-12 form-group col-md-1") {
                    LABEL {
                        +"Published"
                    }
                    DIV("ml-4 mt-1") {
                        CHECKBOX(route::published, withId = true)
                    }
                }
            }

            DIV("form-group") {
                LABEL {
                    +"Page Title"
                }
                INPUT("form-control") {
                    nameValueString(route::pageTitle)
                }
            }

            drawPageWidgets(route, template?.widgets)

            if(p.id >= 0) SUBMIT("btn btn-primary", route::doSave, "Save")
            SUBMIT("btn btn-success", route::doSaveAndContinue, "Save and Continue")

            if(p.id > 0) SUBMIT("btn btn-danger", route::doRemove, "Remove Page") {
                onClick("""return window.confirm('Are you sure?')""")
            }
        }
    }

    private fun GossRendererTypedSelect<Long>.drawParentOptions(parentId: Long?, prefix: String) {
        parents.filter { it.parentId == parentId }.forEach { p ->
            OPTION(p.id, "$prefix${p.title}")
            drawParentOptions(p.id, "$prefix- ")
        }
    }

    private fun hasPageProperties(w: Widget): Boolean = w.properties.any { !it.globalScope } ||
        (w is WidgetContainer && w.children?.any { hasPageProperties(it) } ?: false)

    private fun drawPageWidgets(route: KcmsPageSaveRoute, widgets: List<Widget>?): Unit = namePrefix(route::properties, reset = true) {
        val kcmsPropertiesEditBlock = KcmsPropertiesEditBlock(route, properties)

        widgets?.filter { hasPageProperties(it) }?.forEach { w ->
            H5 { +w.title }
            DIV("ml-4") {
                val rows = w.propertiesRows
                if(rows != null) rows.filter { it.any { !it.globalScope } }.forEach { list ->
                    DIV("row") {
                        list.filterNot { it.globalScope }.forEach { p ->
                            DIV("col-12 col-md") {
                                kcmsPropertiesEditBlock.draw(listOf(p))
                            }
                        }
                    }
                } else {
                    kcmsPropertiesEditBlock.draw(w.properties.filterNot { it.globalScope })
                }

                if(w is WidgetContainer) drawPageWidgets(route, w.children)
            }
        }
    }
}