package kcms.pages

import kcms.common.CommonService
import org.springframework.stereotype.Service

@Service
class PageTemplatesService(
    val templates: List<PageTemplate>,
    val pagesRepository: PagesRepository,
    val pagePropertyRepository: PagePropertyRepository,
) : CommonService() {
    private val templatesMap = templates.associateBy { it.id }

    fun getTemplate(id: String): PageTemplate? = templatesMap[id]

    fun getPageProperties(pageId: Long) = pagePropertyRepository.findByIdPageId(pageId).groupBy { it.id.widgetId }.mapValues {
        it.value.associateBy { it.id.propertyId }
    }

    fun getPage(slug: String) = pagesRepository.findBySlug(slug)
}