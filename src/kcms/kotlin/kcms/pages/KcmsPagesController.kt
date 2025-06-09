package kcms.pages

import kcms.common.Caches
import kcms.common.CommonService
import kcms.common.nullIfBlank
import kcms.common.orNull
import kcms.files.PageFileRepository
import kcms.ui.cms.PagedData
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import java.time.LocalDate
import javax.persistence.EntityManager

@Controller
@RouteHandler
class KcmsPagesController(
    val pagesRepository: PagesRepository,
    val pagePropertyRepository: PagePropertyRepository,
    val pageTemplatesService: PageTemplatesService,
    val pageFileRepository: PageFileRepository,
) : CommonService() {
    class KcmsTemplatesListRoute : GetRoute

    @RouteHandler
    fun templatesList(route: KcmsTemplatesListRoute): View {
        return KcmsTemplatesListPage(pageTemplatesService.templates)
    }

    @RouteHandler
    fun template(route: KcmsTemplateRoute): View {
        return KcmsTemplatePage(
            pageTemplatesService.getTemplate(route.templateId)!!,
            properties = pagePropertyRepository.findByIdPageId(0L).groupBy { it.id.widgetId }.mapValues {
                it.value.associateBy { it.id.propertyId }
            }
        )
    }

    @RouteHandler
    fun templateSave(route: KcmsTemplateSaveRoute): ModelAndView {
        if(route.doSave != null) {
            transaction { em ->
                savePageProperties(em, route, 0L)
            }
            Caches.instance.reset()
        }

        return ModelAndView(redirect(KcmsTemplatesListRoute()))
    }

    data class KcmsPagesListRoute(
        val query: String? = null,
        val searchContent: Boolean? = null,
        val page: Int? = null,
    ) : GetRoute

    @RouteHandler
    fun pagesList(
        route: KcmsPagesListRoute
    ): View {
        val pages = pageTemplatesService.searchPages(
            query = route.query,
            templateId = null,
            checkProperties = route.searchContent == true
        )

        return KcmsPagesListPage(route, PagedData.of(
            list = pages.toList(),
            page = route.page ?: 1,
            pageSize = 20
        ))
    }

    data class KcmsPageRoute(val id: Long) : GetRoute

    @RouteHandler
    fun page(
        route: KcmsPageRoute
    ): View {
        val p = pagesRepository.findById(route.id).orNull() ?: Page(
            id = -1L,
            slug = "",
            title = "",
            template = ""
        )

        return KcmsPagePage(
            templates = pageTemplatesService.templates,
            parents = pagesRepository.findAll().filter {
                pageTemplatesService.getTemplate(it.template) is CouldBeParentPageTemplate
            },
            template = pageTemplatesService.getTemplate(p.template),
            p = p,
            properties = pagePropertyRepository.findByIdPageId(p.id).groupBy { it.id.widgetId }.mapValues {
                it.value.associateBy { it.id.propertyId }
            },
            files = pageFileRepository.findByPageId(p.id)
        )
    }

    fun savePageProperties(em: EntityManager, route: WidgetPropertiesSaveRoute, pageId: Long) {
        route.properties.forEach { (widgetId, props) ->
            props.forEach { (propertyId, value) ->
                em.merge(PageProperty(
                    id = PagePropertyId(
                        pageId = pageId,
                        widgetId = widgetId,
                        propertyId = propertyId,
                    ),
                    text = value,
                    number = value.toBigDecimalOrNull(),
                    date = try { LocalDate.parse(value) }catch(e: Exception) { null }
                ))
            }
        }
        route.listProperties.forEach { (widgetId, props) ->
            props.forEach { (propertyId, values) ->
                em.merge(PageProperty(
                    id = PagePropertyId(
                        pageId = pageId,
                        widgetId = widgetId,
                        propertyId = propertyId,
                    ),
                ).also { it.asList = values.mapNotNull { it.nullIfBlank() } })
            }
        }
        route.enumMapProperties.forEach { (widgetId, props) ->
            props.map { p ->
                p.key.split("@", limit = 2).let {
                    Triple(it[0], it[1], p.value)
                }
            }.groupBy { it.first }.forEach { (propertyId, map) ->
                val map = map.map {
                    it.second.toLongOrNull() to it.third.nullIfBlank()
                }.filter {
                    it.first != null && it.second != null
                }.associate {
                    it.first!! to it.second!!
                }

                em.merge(PageProperty(
                    id = PagePropertyId(
                        pageId = pageId,
                        widgetId = widgetId,
                        propertyId = propertyId,
                    ),
                ).also { it.asMap = map } )
            }
        }
    }

    @RouteHandler
    fun pageSave(route: KcmsPageSaveRoute): String {
        val p = pagesRepository.findById(route.pageId).orNull() ?: Page(
            id = pagesRepository.nextPageId(),
            slug = "",
            title = "",
            template = ""
        )

        p.title = route.pageTitle
        p.slug = route.pageSlug
        p.template = route.pageTemplate
        p.parentId = route.parentId
        p.published = route.published == true

        if(route.doRemove != null) {
            transaction {
                pagesRepository.deleteById(p.id)
            }
        }

        if(route.doSave != null || route.doSaveAndContinue != null) {
            transaction { em ->
                pagesRepository.save(p)
                savePageProperties(em, route, p.id)
            }
            Caches.instance.reset()
        }

        return redirect(if(route.doSaveAndContinue != null) KcmsPageRoute(p.id) else KcmsPagesListRoute())
    }


}