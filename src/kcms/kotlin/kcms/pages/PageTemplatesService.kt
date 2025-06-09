package kcms.pages

import com.google.common.cache.CacheBuilder
import kcms.common.Caching
import kcms.common.CommonService
import kcms.common.chunkedLists
import kcms.common.nullIfBlank
import kcms.common.orNull
import kcms.files.PageFilesService
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

@Service
class PageTemplatesService(
    val templates: List<PageTemplate>,
    val pagesRepository: PagesRepository,
    val pagePropertyRepository: PagePropertyRepository,
    val pageFilesService: PageFilesService,
) : CommonService(), Caching {
    private val templatesMap = templates.associateBy { it.id }

    private val propertiesByPageCache = CacheBuilder.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .softValues()
        .build<Long, Map<String, Map<String, PageProperty>>>()

    override fun resetCaches() {
        propertiesByPageCache.invalidateAll()
    }

    fun getTemplate(id: String): PageTemplate? = templatesMap[id]

    fun getPageProperties(pageId: Long) = propertiesByPageCache.get(pageId) {
        pagePropertyRepository.findByIdPageId(pageId).map { it.copy() }.groupBy { it.id.widgetId }.mapValues {
            it.value.associateBy { it.id.propertyId }
        }
    }

    fun getPagesProperties(pageIds: List<Long>): Map<Long, Map<String, Map<String, PageProperty>>> {
        val result = HashMap<Long, Map<String, Map<String, PageProperty>>>()

        val missedIds = pageIds.filter { pageId ->
            val properties = propertiesByPageCache.getIfPresent(pageId)
            if(properties != null) result[pageId] = properties
            properties == null
        }

        if(missedIds.isNotEmpty()) {
            val pagesProperties = pagePropertyRepository.findByIdPageIdIn(missedIds)
                .map { it.copy() }
                .groupBy { it.id.pageId }
                .mapValues { e ->
                    e.value.groupBy { it.id.widgetId }.mapValues {
                        it.value.associateBy { it.id.propertyId }
                    }
                }

            missedIds.forEach { pageId ->
                val properties = pagesProperties[pageId] ?: emptyMap()
                result[pageId] = properties
                propertiesByPageCache.put(pageId, properties)
            }
        }

        return result
    }

    fun getPage(slug: String) = pagesRepository.findBySlug(slug)

    fun getPage(pageId: Long) = pagesRepository.findById(pageId).orNull()

    private fun prepareQuery(query: String?): List<String> {
        return query.nullIfBlank()?.let { query ->
            Regex("(\\p{javaUnicodeIdentifierPart}{2,})").findAll(query).map {
                it.groupValues[1]
            }.toList()
        } ?: emptyList()
    }

    private fun matchQuery(text: String?, query: List<String>): Boolean {
        if(text == null) return false
        return query.all { q -> text.contains(q, ignoreCase = true) }
    }

    fun searchPages(
        query: String? = null,
        templateId: String? = null,
        checkProperties: Boolean = false,
    ): Sequence<Page> {
        val query = prepareQuery(query)

        return pagesRepository.chunkedLists(100).flatMap { pages ->
            val properties = if(checkProperties) {
                pagePropertyRepository.findByIdPageIdIn(pages.map { it.id }).groupBy { it.id.pageId }
            } else null

            pages.filter { p ->
                val pp = properties?.get(p.id) ?: emptyList()

                (templateId == null || p.template == templateId) && (
                    matchQuery(p.title, query) || matchQuery(p.slug, query) || (properties != null && pp.any { matchQuery(it.text, query) })
                )
            }
        }
    }

    fun getChildren(request: HttpServletRequest, pageId: Long, limit: Int? = null): List<PageTemplateRenderContext> {
        val children = pagesRepository.findByParentId(pageId).filter { it.published }
        val pageIds = children.map { it.id }
        val properties = getPagesProperties(pageIds)
        val files = pageFilesService.getPagesFiles(pageIds)

        val limited = limit?.let {
            children.take(it)
        } ?: children

        return limited.map { page ->
            PageTemplateRenderContext(
                request = request,
                page = page,
                rootProperties = getPageProperties(0L),
                pageProperties = properties[page.id] ?: emptyMap(),
                pageFiles = files[page.id] ?: emptyList()
            )
        }
    }

    @PostConstruct
    fun postConstruct() {
        instance = this
    }

    companion object {
        lateinit var instance: PageTemplatesService
    }
}