package kcms.pages

import kcms.common.Caches
import kcms.common.CommonService
import kcms.common.nullIfBlank
import kcms.common.orNull
import kcms.files.PageFileRepository
import kcms.ui.cms.PagedData
import kcms.ui.cms.WithOrdersRoute
import kcms.widgets.SitePropertiesDescriptor
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
    val sitePropertyRepository: SitePropertyRepository,
    val pageTemplatesService: PageTemplatesService,
    val pageFileRepository: PageFileRepository,
    val slugGenerators: SlugGenerators,
    val sitePropertiesDescriptors: List<SitePropertiesDescriptor>,
) : CommonService() {

    class KcmsSiteSettingsRoute : GetRoute

    @RouteHandler
    fun settings(route: KcmsSiteSettingsRoute): View {
        return KcmsSiteSettingsPage(
            sitePropertiesDescriptors
        )
    }

    @RouteHandler
    fun template(route: KcmsSitePropertiesRoute): View {
        return KcmsSitePropertiesPage(
            sitePropertiesDescriptors.first { it.javaClass.simpleName == route.bean },
            properties = sitePropertyRepository.findAll().associateBy { it.key }
        )
    }

    @RouteHandler
    fun templateSave(route: KcmsSitePropertiesSaveRoute): ModelAndView {
        if(route.doSave != null) {
            transaction { em ->
                getPageProperties(route).forEach {
                    em.merge(it)
                }
            }
            Caches.instance.resetAll()
        }

        return ModelAndView(redirect(KcmsSiteSettingsRoute()))
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
            templateIds = route.templateId.nullIfBlank()?.let { setOf(it) },
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

    fun getPageProperties(route: WidgetPropertiesSaveRoute): List<SiteProperty> {
        return route.properties.map { (propertyId, value) ->
            SiteProperty(
                key = propertyId,
                text = value,
                number = value.toBigDecimalOrNull(),
                date = try { LocalDate.parse(value) }catch(e: Exception) { null }
            )
        } + route.listProperties.map { (propertyId, values) ->
            SiteProperty(
                key = propertyId,
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

            SiteProperty(
                key = propertyId,
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

        val properties = getPageProperties(route).associateBy { it.key }

        p.title = route.pageTitle
        p.templateId = route.pageTemplate
        p.parentId = route.parentId
        p.published = route.published == true
        p.slug = slugGenerators.generators[p.templateId]?.generateSlug(p, object : WidgetRenderContext {
            override fun getProperty(propertyKey: String): KcmsProperty? = properties[propertyKey]
        }) ?: route.pageSlug

        if(route.doRemove != null) {
            transaction {
                pagesRepository.deleteById(p.id)
            }
            Caches.instance.resetAll()
        }

        if(route.doSave != null || route.doSaveAndContinue != null) {
            transaction { em ->
                pagesRepository.save(p)
                properties.values.forEach {
                    em.merge(PageProperty(
                        id = PagePropertyId(pageId = p.id, propertyId = it.key),
                        text = it.text,
                        number = it.number,
                        date = it.date
                    ))
                }
            }
            Caches.instance.resetAll()
        }

        return redirect(if(route.doSaveAndContinue != null) KcmsPageRoute(p.id) else KcmsPagesListRoute())
    }

    data class KcmsPagesOrderSaveRoute(
        override val orders: MutableMap<Long, Int> = HashMap(),
    ) : PutRoute, WithOrdersRoute

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

        Caches.instance.resetAll()

        return null
    }

}