package kcms.pages

import kcms.common.ifTrue
import kcms.files.PageFilesService
import kcms.ui.KcmsGossRenderer
import kcms.ui.SiteMapView
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.View
import java.io.File
import java.net.URLConnection
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class PublicPagesController(
    val pagesRepository: PagesRepository,
    val pageTemplatesService: PageTemplatesService,
    val pageFilesService: PageFilesService,
) {
    @Value("\${cms.url-base}")
    lateinit var urlBase: String

    @GetMapping("/**")
    fun page(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Any? {
        val uri = request.requestURI

        return if(checkForFile(response, uri)) {
            null
        } else {
            pageView(request, response, uri)
        }
    }

    private fun checkForFile(response: HttpServletResponse, uri: String): Boolean {
        if(KcmsGossRenderer.isDevMode && (checkFile(response, "src/kcms/resources/static/$uri") || checkFile(response, "src/project/resources/static/$uri"))) {
            return true
        }

        val resource = ClassPathResource("static$uri")
        if(resource.exists() && resource.file.isFile) {
            var mimeType = URLConnection.guessContentTypeFromName(uri)
            if(mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE
            }

            response.setDateHeader(HttpHeaders.EXPIRES, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365))
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + (365L * 24 * 60 * 60))

            response.contentType = mimeType
            resource.inputStream.use {
                StreamUtils.copy(it, response.outputStream.buffered(64000))
            }
            return true
        }

        return false
    }

    private fun checkFile(response: HttpServletResponse, path: String): Boolean {
        val file = File(path)
        if(file.exists() && file.isFile) {
            var mimeType = URLConnection.guessContentTypeFromName(path)
            if(mimeType == null) {
                if(path.endsWith(".webmanifest")) {
                    mimeType = "application/manifest+json"
                } else if(path.endsWith(".ico")) {
                    mimeType = "image/x-icon"
                } else mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE
            }

            response.setHeader(HttpHeaders.PRAGMA, "no-cache")
            response.setHeader(HttpHeaders.EXPIRES, "0")
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")

            response.contentType = mimeType

            file.inputStream().use {
                StreamUtils.copy(it, response.outputStream.buffered(64000))
            }

            return true
        }

        return false
    }

    fun pageView(
        request: HttpServletRequest,
        response: HttpServletResponse,
        uri: String
    ): View? {
        val page = pageTemplatesService.getPage(uri)
            ?: pageTemplatesService.getPage(URLDecoder.decode(uri, "utf-8"))
            ?: pageTemplatesService.getPage(uri.trimEnd('/'))
            ?: pageTemplatesService.getPage(URLDecoder.decode(uri, "utf-8").trimEnd('/'))

        return page?.let { page ->
            page.published.ifTrue { pageView(request, page) }
        } ?: run {
            response.status = 404
            pageTemplatesService.getPage(-404L)?.let {
                it.published.ifTrue { pageView(request, it) }
            }
        }
    }

    private fun pageView(request: HttpServletRequest, page: Page) = page.template?.view(
        request,
        PageTemplateRenderContext(
            page = page,
            siteProperties =  pageTemplatesService.getSiteProperties(),
            pageProperties = pageTemplatesService.getPageProperties(page.id),
            pageFiles = pageFilesService.getPageFiles(page.id)
        )
    )

    @GetMapping("/sitemap.xml")
    fun siteMap(): View {
        return SiteMapView(
            urlBase,
            pagesRepository.findAll().filter {
                it.id >= 0 && it.published
            }
        )
    }
}