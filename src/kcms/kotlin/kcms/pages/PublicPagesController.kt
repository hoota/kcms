package kcms.pages

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import java.net.URLConnection
import java.net.URLDecoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class PublicPagesController(
    val pageTemplatesService: PageTemplatesService,
) {
    @GetMapping("/**")
    fun page(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Any? {
        val uri = request.requestURI
        val resource = ClassPathResource("static$uri")
        if(resource.exists() && resource.file.isFile) {
            var mimeType: String = URLConnection.guessContentTypeFromName(uri)
            if(mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE
            }

            response.contentType = mimeType
            StreamUtils.copy(resource.inputStream, response.outputStream)
            return null
        }

        val page = pageTemplatesService.getPage(uri)
            ?: pageTemplatesService.getPage(URLDecoder.decode(uri, "utf-8"))
            ?: pageTemplatesService.getPage(uri.trimEnd('/'))
            ?: pageTemplatesService.getPage(URLDecoder.decode(uri, "utf-8").trimEnd('/'))

        return page?.let { page ->
            pageTemplatesService.getTemplate(page.template)?.let { template ->
                template.view(
                    PageTemplateRenderContext(
                        page = page,
                        rootProperties =  pageTemplatesService.getPageProperties(0),
                        pageProperties = pageTemplatesService.getPageProperties(page.id)
                    )
                )
            }
        } ?: run {
            response.status = 404
            null
        }
    }
}