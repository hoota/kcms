package kcms.pages

import kcms.enums.CmsEnumCategoryNewValueRoute
import kcms.files.CmsImageScale
import kcms.files.CmsImageScaleType
import kcms.files.PageFile
import kcms.ui.cms.CommonCMSPage
import kcms.ui.cms.MenuModule
import kcms.widgets.WidgetContainer
import kcms.widgets.Widget
import kiss.gossr.spring.PostRoute
import org.springframework.stereotype.Component

data class CmsPageSaveRoute(
    val pageId: Long,
    val pageSlug: String,
    val pageTitle: String,
    val pageTemplate: String,
    val parentId: Long?,
    val properties: MutableMap<String, MutableMap<String, String>> = HashMap(),
    val doSave: String? = null,
    val doRemove: String? = null,
) : PostRoute

@Component
data class CmsImageScaleH100(
    override val size: Int = 100,
    override val type: CmsImageScaleType = CmsImageScaleType.HEIGHT
) : CmsImageScale

@Component
data class CmsImageScaleW100(
    override val size: Int = 100,
    override val type: CmsImageScaleType = CmsImageScaleType.WIDTH
) : CmsImageScale

class CMSPagePage(
    val templates: List<PageTemplate>,
    val parents: List<Page>,
    val p: Page,
    val template: PageTemplate?,
    val properties: Map<String, Map<String, PageProperty>>,
    val files: List<PageFile>,
) : CommonCMSPage(
    title = "Page #${p.id} // ${p.title}",
    module = MenuModule.PAGES
) {

    override fun pageBody() {
        FORM(CmsPageSaveRoute(
            pageId = p.id,
            pageSlug = p.slug,
            pageTitle = p.title,
            pageTemplate = p.template,
            parentId = p.parentId
        )) { route ->
            HIDDEN(route::pageId)

            DIV("form-group") {
                LABEL {
                    +"Parent"
                }
                SELECT(route::parentId) {
                    classes("form-control")
                    OPTION("-- no parent --")
                    parents.forEach { p ->
                        OPTION(p.id, p.title)
                    }
                }
            }

            DIV("form-group") {
                LABEL {
                    +"Page Slug (URL)"
                }
                INPUT("form-control") {
                    nameValueString(route::pageSlug)
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

            DIV("form-group") {
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

            H4 { +"Page Widgets" }
            drawPageWidgets(route, template?.widgets)

            SUBMIT("btn btn-primary", route::doSave, "Save")

            if(p.id > 0) SUBMIT("btn btn-danger", route::doRemove, "Remove Page") {
                onClick("""return window.confirm('Are you sure?')""")
            }
        }

        if(p.id >= 0) drawFiles()
    }

    private fun drawFiles() {
        FORM(CMSPagesController.CmsPageFilesUploadRoute(pageId = p.id)) { route ->
            classes("mt-3")
            style("float: right;")

            HIDDEN(route::pageId)

            DIV("input-group") {
                DIV("input-group-prepend") {
                    SPAN("input-group-text") {
                        id("inputGroupFileAddon01")
                        +"Upload"
                    }
                }
                DIV("custom-file") {
                    INPUT("custom-file-input") {
                        type("file")
                        id("inputGroupFile01")
                        name(route::files.name)
                        multiple(true)
                        onChange("this.form.submit()")
                        attr("aria-describedby", "inputGroupFileAddon01")
                    }
                    LABEL("custom-file-label") {
                        forAttr("inputGroupFile01")
                        +"Choose file"
                    }
                }
            }
        }

        H4("mt-4") { +"Files" }

        TABLE("table") {
            TBODY {
                files.forEach { f ->
                    TR {
                        TD { +f.id.toString() }
                        TD { +f.type }
                        TD {
                            A {
                                target("_blank")
                                href(f.url())

                                if(f.type.image) {
                                    IMG(src = f.urlWithHeight(100), height = 100)
                                } else {
                                    +f.origName
                                }
                            }
                        }
                        TD {
                            FORM(CMSPagesController.CmsPageFileRemoveRoute(pageId = f.pageId, fileId = f.id)) { route ->
                                style("float: right;")
                                HIDDEN(route::pageId)
                                HIDDEN(route::fileId)

                                SUBMIT("btn btn-sm btn-danger", "remove") {
                                    onClick("return window.confirm('Are you sure?')")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasPageProperties(w: Widget): Boolean = w.properties.any { !it.shared } ||
        (w is WidgetContainer && w.children?.any { hasPageProperties(it) } ?: false)

    private fun drawPageWidgets(route: CmsPageSaveRoute, widgets: List<Widget>?) {
        widgets?.filter { hasPageProperties(it) }?.forEach { w ->
            B { +w.title }
            DIV("ml-4") {
                namePrefix(route::properties, w.id) {
                    CmsPropertiesEditBlock().draw(properties, w.id, w.properties.filter { !it.shared })
                }

                if(w is WidgetContainer) drawPageWidgets(route, w.children)
            }
        }
    }
}