package kcms.files

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import java.io.File
import java.net.URLConnection
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class PublicFilesController(
    val pageFilesService: PageFilesService
) {
    @GetMapping("/files/**")
    fun getFile(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val uri = request.requestURI.replace("/../", "/")
        val file = File(".$uri").absoluteFile

        if(!file.exists()) {
            if(!pageFilesService.scale(file)) {
                response.status = 404
                return
            }
        }

        var mimeType: String = URLConnection.guessContentTypeFromName(uri)
        if(mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE
        }

        response.contentType = mimeType
        StreamUtils.copy(file.inputStream(), response.outputStream)
    }
}