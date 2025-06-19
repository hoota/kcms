package kcms.pages

import kcms.common.Caches
import kcms.common.CommonService
import kcms.common.nullIfBlank
import kcms.common.orNull
import kcms.files.PageFileRepository
import kcms.ui.cms.PagedData
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PutRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.servlet.http.HttpServletResponse

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
            properties = pagePropertyRepository.findByIdPageId(0L).associateBy { it.id.propertyId }
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
    fun page(
        route: KcmsPageRoute
    ): View {
        val p = pagesRepository.findById(route.id).orNull() ?: Page(
            id = -1L,
            slug = "",
            title = "",
            template = ""
        )

        return when(route.tab) {
            KcmsPageTabs.CHILDREN -> KcmsPageChildrenPage(
                p = p,
                children = pagesRepository.findByParentId(p.id)
            )
            KcmsPageTabs.FILES -> KcmsPageFilesPage(
                p = p,
                files = pageFileRepository.findByPageId(p.id)
            )
            else -> KcmsPagePage(
                templates = pageTemplatesService.templates,
                parents = pageTemplatesService.getPagesTree().values.map { it.p },
                template = pageTemplatesService.getTemplate(p.template),
                p = p,
                properties = pagePropertyRepository.findByIdPageId(p.id).associateBy { it.id.propertyId },
            )
        }
    }

    fun savePageProperties(em: EntityManager, route: WidgetPropertiesSaveRoute, pageId: Long) {
        route.properties.forEach { (propertyId, value) ->
            em.merge(PageProperty(
                id = PagePropertyId(
                    pageId = pageId,
                    propertyId = propertyId,
                ),
                text = value,
                number = value.toBigDecimalOrNull(),
                date = try { LocalDate.parse(value) }catch(e: Exception) { null }
            ))
        }
        route.listProperties.forEach { (propertyId, values) ->
            em.merge(PageProperty(
                id = PagePropertyId(
                    pageId = pageId,
                    propertyId = propertyId,
                ),
            ).also { it.asList = values.mapNotNull { it.nullIfBlank() } })
        }
        route.enumMapProperties.map { p ->
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
                    propertyId = propertyId,
                ),
            ).also { it.asMap = map } )
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