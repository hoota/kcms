package kcms.pages

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization
import kiss.gossr.GossRendererTypedSelect
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute

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
    title = if(p.id >= 0) "${KcmsInternationalization.instance.page} #${p.id} // ${p.title}" else KcmsInternationalization.instance.newPage,
    module = MenuModule.PAGES,
    showTitleAsHeader = false
) {

    private fun drawTabs() {
        DIV("nav nav-tabs mt-1 mb-1") {
            A("nav-item nav-link active show") {
                href("#")
                +i18n.properties
            }
            A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.FILES))
                +i18n.files
            }
            if(!noChildren) A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.CHILDREN))
                +i18n.children
            }
        }
    }

    override fun pageBody() {
        includeTrumbowyg()

        if(p.id >= 0) {
            H3 {
                classes("mt-3 page-title")
                A {
                    href(p.slug)
                    +"${i18n.page} #${p.id}"
                }
                +" // ${p.title}"
            }

            drawTabs()
        } else {
            H3 {
                classes("mt-3 page-title")
                +i18n.newPage
            }
        }

        FORM(KcmsPageSaveRoute(
            pageId = p.id,
            pageSlug = p.slug,
            pageTitle = p.title,
            pageTemplate = p.templateId,
            parentId = p.parentId,
            published = p.published,
        )) { route ->
            HIDDEN(route::pageId)

            DIV("row") {
                DIV("col-12 form-group col-md") {
                    LABEL {
                        +i18n.pageSlug
                    }
                    INPUT("form-control") {
                        nameValueString(route::pageSlug)
                        readonly(SlugGenerators.instance.generators.containsKey(p.templateId))
                    }
                }
                DIV("col-12 form-group col-md") {
                    LABEL {
                        +i18n.parent
                    }
                    SELECT(route::parentId) {
                        classes("form-control")
                        OPTION("-- ${i18n.noParent} --")
                        this.drawParentOptions(null, "")
                    }
                }
                DIV("col-12 form-group col-md") {
                    LABEL {
                        +i18n.template
                    }
                    SELECT(route::pageTemplate) {
                        classes("form-control")
                        templates.forEach { t ->
                            OPTION(t.templateId)
                        }
                    }
                }
                DIV("col-12 form-group col-md-1") {
                    LABEL {
                        +i18n.published
                    }
                    DIV("ml-4 mt-1") {
                        CHECKBOX(route::published, withId = true)
                    }
                }
            }

            DIV("form-group") {
                LABEL {
                    +i18n.title
                }
                INPUT("form-control") {
                    required()
                    nameValueString(route::pageTitle)
                }
            }

            KcmsPropertiesEditBlock(route, properties).drawWidgets(template?.widgets)

            if(p.id >= 0) SUBMIT("btn btn-primary", route::doSave, i18n.save)
            SUBMIT("btn btn-success", route::doSaveAndContinue, i18n.saveAndContinue)

            if(p.id > 0) SUBMIT("btn btn-danger", route::doRemove, i18n.removePage) {
                onClick("""return window.confirm(${toJson(i18n.areYouSure)})""")
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