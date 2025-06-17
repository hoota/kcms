package kcms.pages

import kcms.common.ifTrue
import kcms.files.PageFilesService
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.View
import java.net.URLConnection
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class PublicPagesController(
    val pageTemplatesService: PageTemplatesService,
    val pageFilesService: PageFilesService,
) {
    @GetMapping("/**")
    fun page(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Any? {
        val uri = request.requestURI
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
            return null
        }

        return pageView(request, response, uri)
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

    private fun pageView(request: HttpServletRequest, page: Page) = pageTemplatesService.getTemplate(page.template)?.view(
        PageTemplateRenderContext(
            request = request,
            page = page,
            rootProperties =  pageTemplatesService.getPageProperties(0),
            pageProperties = pageTemplatesService.getPageProperties(page.id),
            pageFiles = pageFilesService.getPageFiles(page.id)
        )
    )
}