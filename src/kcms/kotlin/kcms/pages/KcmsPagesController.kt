package kcms.pages

import kcms.common.Caches
import kcms.common.CommonService
import kcms.common.nullIfBlank
import kcms.common.orNull
import kcms.files.PageFileRepository
import kcms.ui.cms.PagedData
import kcms.widgets.WidgetRenderContext
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PutRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import java.time.LocalDate
import javax.servlet.http.HttpServletResponse

@Controller
@RouteHandler
class KcmsPagesController(
    val pagesRepository: PagesRepository,
    val pagePropertyRepository: PagePropertyRepository,
    val pageTemplatesService: PageTemplatesService,
    val pageFileRepository: PageFileRepository,
    val slugGenerators: SlugGenerators
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
            properties = pagePropertyRepository.findByIdPageId(0L).associateBy { it.id.propertyId }
        )
    }

    @RouteHandler
    fun templateSave(route: KcmsTemplateSaveRoute): ModelAndView {
        if(route.doSave != null) {
            transaction { em ->
                getPageProperties(route, 0L).forEach {
                    em.merge(it)
                }
            }
            Caches.instance.reset()
        }

        return ModelAndView(redirect(KcmsTemplatesListRoute()))
    }

    data class KcmsPagesListRoute(
        val query: String? = null,
        val searchContent: Boolean? = null,
        val templateId: String? = null,
        val parentId: Long? = null,
        val page: Int? = null,
    ) : GetRoute

    @RouteHandler
    fun pagesList(
        route: KcmsPagesListRoute
    ): View {
        val pages = pageTemplatesService.searchPages(
            query = route.query,
            templateIds = route.templateId?.let { setOf(it) },
            parentIds = route.parentId?.let { setOf(it) },
            checkProperties = route.searchContent == true
        )

        return KcmsPagesListPage(
            route = route,
            templates = pageTemplatesService.templates,
            parents = pageTemplatesService.getPagesTree().values.map { it.p },
            PagedData.of(
                list = pages.toList(),
                page = route.page ?: 1,
                pageSize = 20
            )
        )
    }

    @RouteHandler
    fun newPage(
        route: KcmsPageNewRoute
    ): View {
        val p = Page(
            id = -1L,
            slug = route.slug.nullIfBlank() ?: "",
            title = "",
            templateId = route.templateId.nullIfBlank() ?: "",
            parentId = route.parentId
        )

        return KcmsPagePage(
            templates = pageTemplatesService.templates,
            parents = pageTemplatesService.getPagesTree().values.map { it.p },
            template = pageTemplatesService.getTemplate(p.templateId),
            p = p,
            properties = pagePropertyRepository.findByIdPageId(p.id).associateBy { it.id.propertyId },
        )
    }

    @RouteHandler
    fun page(
        route: KcmsPageRoute
    ): View {
        val p = pagesRepository.findById(route.id).get()
        val children = pagesRepository.findByParentId(p.id)

        return when {
            route.tab == KcmsPageTabs.CHILDREN && children.isNotEmpty() -> {
                KcmsPageChildrenPage(
                    p = p,
                    children = children
                )
            }
            route.tab == KcmsPageTabs.FILES -> KcmsPageFilesPage(
                p = p,
                files = pageFileRepository.findByPageId(p.id),
                noChildren = children.isEmpty()
            )
            else -> KcmsPagePage(
                templates = pageTemplatesService.templates,
                parents = pageTemplatesService.getPagesTree().values.map { it.p },
                template = pageTemplatesService.getTemplate(p.templateId),
                p = p,
                properties = pagePropertyRepository.findByIdPageId(p.id).associateBy { it.id.propertyId },
                noChildren = children.isEmpty()
            )
        }
    }

    fun getPageProperties(route: WidgetPropertiesSaveRoute, pageId: Long): List<PageProperty> {
        return route.properties.map { (propertyId, value) ->
            PageProperty(
                id = PagePropertyId(
                    pageId = pageId,
                    propertyId = propertyId,
                ),
                text = value,
                number = value.toBigDecimalOrNull(),
                date = try { LocalDate.parse(value) }catch(e: Exception) { null }
            )
        } + route.listProperties.map { (propertyId, values) ->
            PageProperty(
                id = PagePropertyId(
                    pageId = pageId,
                    propertyId = propertyId,
                ),
            ).also { it.asList = values.mapNotNull { it.nullIfBlank() } }
        } + route.enumMapProperties.map { p ->
            p.key.split("@", limit = 2).let {
                Triple(it[0], it[1], p.value)
            }
        }.groupBy { it.first }.map { (propertyId, map) ->
            val map = map.map {
                it.second.toLongOrNull() to it.third.nullIfBlank()
            }.filter {
                it.first != null && it.second != null
            }.associate {
                it.first!! to it.second!!
            }

            PageProperty(
                id = PagePropertyId(
                    pageId = pageId,
                    propertyId = propertyId,
                ),
            ).also { it.asMap = map }
        }
    }

    @RouteHandler
    fun pageSave(route: KcmsPageSaveRoute): String {
        val p = pagesRepository.findById(route.pageId).orNull() ?: Page(
            id = pagesRepository.nextPageId(),
            slug = "",
            title = "",
            templateId = ""
        )

        val properties = getPageProperties(route, p.id).associateBy { it.id.propertyId }

        p.title = route.pageTitle
        p.templateId = route.pageTemplate
        p.parentId = route.parentId
        p.published = route.published == true
        p.slug = slugGenerators.generators[p.templateId]?.generateSlug(p, object : WidgetRenderContext {
            override fun getProperty(propertyKey: String): PageProperty? = properties[propertyKey]
        }) ?: route.pageSlug

        if(route.doRemove != null) {
            transaction {
                pagesRepository.deleteById(p.id)
            }
        }

        if(route.doSave != null || route.doSaveAndContinue != null) {
            transaction { em ->
                pagesRepository.save(p)
                properties.values.forEach {
                    em.merge(it)
                }
            }
            Caches.instance.reset()
        }

        return redirect(if(route.doSaveAndContinue != null) KcmsPageRoute(p.id) else KcmsPagesListRoute())
    }

    data class KcmsPagesOrderSaveRoute(
        val orders: MutableMap<Long, Int> = HashMap(),
    ) : PutRoute

    @RouteHandler
    fun orderSave(
        response: HttpServletResponse,
        route: KcmsPagesOrderSaveRoute
    ): Any? {
        transaction {
            route.orders.forEach { (fileId, order) ->
                pagesRepository.findById(fileId).ifPresent {
                    it.order = order
                }
            }
        }

        Caches.instance.reset()

        return null
    }

}