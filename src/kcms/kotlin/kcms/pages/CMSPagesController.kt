package kcms.pages

import kcms.common.CommonService
import kcms.common.orNull
import kcms.files.PageFileRepository
import kcms.files.PageFilesService
import kcms.ui.cms.PagedData
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.MultipartPostRoute
import kiss.gossr.spring.PostRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import java.time.LocalDate

@Controller
@RouteHandler
class CMSPagesController(
    val pagesRepository: PagesRepository,
    val pagePropertyRepository: PagePropertyRepository,
    val pageTemplatesService: PageTemplatesService,
    val pageFileRepository: PageFileRepository,
    val pageFilesService: PageFilesService,
) : CommonService() {
    class CmsTemplatesListRoute : GetRoute

    @RouteHandler
    fun templatesList(route: CmsTemplatesListRoute): View {
        return CmsTemplatesListPage(pageTemplatesService.templates)
    }

    @RouteHandler
    fun template(route: CmsTemplateRoute): View {
        return CmsTemplatePage(
            pageTemplatesService.getTemplate(route.templateId)!!,
            properties = pagePropertyRepository.findByIdPageId(0L).groupBy { it.id.widgetId }.mapValues {
                it.value.associateBy { it.id.propertyId }
            }
        )
    }

    @RouteHandler
    fun templateSave(route: CmsTemplateSaveRoute): ModelAndView {
        if(route.doSave != null) {
            transaction { em ->
                route.properties.forEach { (widgetId, props) ->
                    props.forEach { (propertyId, value) ->
                        em.merge(PageProperty(
                            id = PagePropertyId(
                                pageId = 0L,
                                widgetId = widgetId,
                                propertyId = propertyId,
                            ),
                            text = value,
                            number = value.toLongOrNull(),
                            date = try { LocalDate.parse(value) }catch(e: Exception) { null }
                        ))
                    }
                }
            }
            pageFilesService.resetCaches()
        }

        return ModelAndView(redirect(CmsTemplatesListRoute()))
    }

    data class CmsPagesListRoute(
        val query: String? = null,
        val searchContent: Boolean? = null,
        val page: Int? = null,
    ) : GetRoute

    @RouteHandler
    fun pagesList(
        route: CmsPagesListRoute
    ): View {
        val pages = pageTemplatesService.searchPages(
            query = route.query,
            templateId = null,
            checkProperties = route.searchContent == true
        )

        return CMSPagesListPage(route, PagedData.of(
            list = pages.toList(),
            page = route.page ?: 1,
            pageSize = 20
        ))
    }

    data class CmsPageRoute(val id: Long) : GetRoute

    @RouteHandler
    fun page(
        route: CmsPageRoute
    ): View {
        val p = pagesRepository.findById(route.id).orNull() ?: Page(
            id = -1L,
            slug = "",
            title = "",
            template = pageTemplatesService.templates.firstOrNull()?.id ?: ""
        )

        return CMSPagePage(
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

    @RouteHandler
    fun pageSave(route: CmsPageSaveRoute): ModelAndView {
        val p = pagesRepository.findById(route.pageId).orNull() ?: Page(
            id = pagesRepository.nextPageId(),
            slug = "",
            title = "",
            template = ""
        )

        p.title = route.pageTitle
        p.slug = route.pageSlug
        p.template = route.pageTemplate

        if(route.doRemove != null) {
            transaction {
                pagesRepository.deleteById(p.id)
            }
        }

        if(route.doSave != null) {
            transaction { em ->
                pagesRepository.save(p)
                route.properties.forEach { (widgetId, props) ->
                    props.forEach { (propertyId, value) ->
                        em.merge(PageProperty(
                            id = PagePropertyId(
                                pageId = p.id,
                                widgetId = widgetId,
                                propertyId = propertyId,
                            ),
                            text = value,
                            number = value.toLongOrNull(),
                            date = try { LocalDate.parse(value) }catch(e: Exception) { null }
                        ))
                    }
                }
            }
            pageFilesService.resetCaches()
        }

        return ModelAndView(redirect(CmsPagesListRoute()))
    }

    data class CmsPageFileRemoveRoute(
        val pageId: Long,
        val fileId: Long
    ) : PostRoute

    @RouteHandler
    fun removeFile(route: CmsPageFileRemoveRoute): String {

        pageFilesService.removeFile(route.pageId, route.fileId)

        return redirect(CmsPageRoute(route.pageId))
    }

    data class CmsPageFilesUploadRoute(
        val pageId: Long,
        var files: ArrayList<MultipartFile>? = null,
    ) : MultipartPostRoute

    @RouteHandler
    fun uploadFiles(route: CmsPageFilesUploadRoute): String {

        route.files?.forEach { file ->
            pageFilesService.save(route.pageId, file)
        }


        return redirect(CmsPageRoute(route.pageId))
    }
}