package kcms.pages

import com.google.common.cache.CacheBuilder
import kcms.common.Caching
import kcms.common.CommonService
import kcms.common.OneValueCache
import kcms.common.chunkedLists
import kcms.common.nullIfBlank
import kcms.common.orNull
import kcms.files.PageFilesService
import kcms.widgets.PagePropertyDescriptor
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.PostConstruct

class PageTreeNode {
    lateinit var p: Page
    var itemsCount: Int = 0
    val children = ArrayList<PageTreeNode>()

    val myAndChildrenIds: List<Long> by lazy {
        myAndChildrenIds()
    }

    val myAndChildrenItemsCount: Int by lazy {
        myAndChildrenItemsCount()
    }

    private fun myAndChildrenItemsCount(): Int = children.sumOf { it.myAndChildrenItemsCount } + itemsCount

    private fun myAndChildrenIds(): List<Long> = children.flatMap {
        it.myAndChildrenIds()
    }.plus(p.id)
}

@Service
class PageTemplatesService(
    val templates: List<PageTemplate>,
    val pagesRepository: PagesRepository,
    val pagePropertyRepository: PagePropertyRepository,
    val sitePropertyRepository: SitePropertyRepository,
    val pageFilesService: PageFilesService,
) : CommonService(), Caching {
    private val templatesMap = templates.associateBy { it.templateId }

    private val propertiesByPageCache = CacheBuilder.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .softValues()
        .build<Long, Map<String, PageProperty>>()

    private val sitePropertiesCache = OneValueCache(
        timeout = 24,
        timeunit = TimeUnit.HOURS
    ) {
        sitePropertyRepository.findAll().associateBy { it.key }
    }

    private val pagesTree = OneValueCache(
        timeout = 24,
        timeunit = TimeUnit.HOURS
    ) {
        val tree = LinkedHashMap<Long, PageTreeNode>()
        val parentTemplates = templates.filterIsInstance<CouldBeParentPageTemplate>().map { it.templateId }.toSet()
        val parentIds = pagesRepository.findParents().toSet()

        pagesRepository.findAll().sortedBy { it.order }.forEach { p ->
            if(p.id in parentIds || p.templateId in parentTemplates) {
                val node = tree.computeIfAbsent(p.id) { PageTreeNode() }
                node.p = p
                p.parentId?.let { parentId ->
                    tree.computeIfAbsent(parentId) { PageTreeNode() }.children.add(node)
                }
            } else {
                p.parentId?.let { parentId ->
                    tree.computeIfAbsent(parentId) { PageTreeNode() }.itemsCount++
                }
            }
        }

        tree
    }

    override fun resetCaches() {
        propertiesByPageCache.invalidateAll()
        sitePropertiesCache.reset()
        pagesTree.reset()
    }

    fun getPagesTree(): Map<Long, PageTreeNode> = pagesTree.value ?: emptyMap()

    fun getTemplate(id: String): PageTemplate? = templatesMap[id]
    fun getTemplate(p: Page): PageTemplate? = templatesMap[p.templateId]

    fun getPageProperties(pageId: Long) = propertiesByPageCache.get(pageId) {
        pagePropertyRepository.findByIdPageId(pageId).map { it.copy() }.associateBy { it.id.propertyId }
    }

    fun updatePageProperty(pageId: Long, propertyId: String, updater: (PageProperty) -> Unit): PageProperty {
        val p = PagePropertyId(
            pageId = pageId, propertyId = propertyId
        ).let { id ->
            pagePropertyRepository.findById(id).orNull() ?: PageProperty(id)
        }

        updater(p)

        return pagePropertyRepository.save(p)
    }


    fun getSiteProperties(): Map<String, SiteProperty> = sitePropertiesCache.value ?: emptyMap()

    fun getPagesProperties(pageIds: List<Long>): Map<Long, Map<String, PageProperty>> {
        val result = HashMap<Long, Map<String, PageProperty>>()

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
                    e.value.associateBy { it.id.propertyId }
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

    fun getPages(pageIds: List<Long>) = pagesRepository.findByIdIn(pageIds)

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
        templateIds: Set<String>? = null,
        parentIds: Set<Long>? = null,
        checkProperties: Boolean = false,
    ): Sequence<Page> {
        val query = prepareQuery(query)

        return pagesRepository.chunkedLists(100).flatMap { pages ->
            val properties = if(checkProperties) {
                pagePropertyRepository.findByIdPageIdIn(pages.map { it.id }).groupBy { it.id.pageId }
            } else null

            pages.filter { p ->
                val pp = properties?.get(p.id) ?: emptyList()

                (templateIds == null || p.templateId in templateIds) && (parentIds == null || p.parentId in parentIds) &&(
                    matchQuery(p.title, query) || matchQuery(p.slug, query) || (properties != null && pp.any { matchQuery(it.text, query) })
                )
            }
        }
    }

    fun getChildren(parentIds: List<Long>, limit: Int? = null): Sequence<PageTemplateRenderContext> {
        var children = pagesRepository.findByParentIdInOrderByOrder(parentIds)
            .asSequence()
            .filter { it.published }

        if(limit != null) children = children.take(limit)


        return children.chunked(100).flatMap { pages ->
            toPageContext(pages)
        }
    }

    fun toPageContext(pages: List<Page>): List<PageTemplateRenderContext> {
        val pageIds = pages.map { it.id }.toList()
        val properties = getPagesProperties(pageIds)
        val files = pageFilesService.getPagesFiles(pageIds)

        return pages.map { page ->
            PageTemplateRenderContext(
                page = page,
                siteProperties = sitePropertiesCache.value,
                pageProperties = properties[page.id] ?: emptyMap(),
                pageFiles = files[page.id]?.sortedBy { it.order } ?: emptyList()
            )
        }
    }

    @PostConstruct
    fun postConstruct() {
        instance = this
    }

    fun removePageProperty(pageId: Long, descriptor: PagePropertyDescriptor) = transaction {
        pagePropertyRepository.deleteById(PagePropertyId(
            pageId = pageId, propertyId = descriptor.key
        ))
    }

    companion object {
        lateinit var instance: PageTemplatesService
    }
}