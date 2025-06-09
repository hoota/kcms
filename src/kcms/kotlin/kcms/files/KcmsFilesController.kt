package kcms.files

import kcms.common.CommonService
import kcms.common.nullIfBlank
import kcms.pages.KcmsPagesController
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.MultipartPostRoute
import kiss.gossr.spring.PostRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.View

@Component
@RouteHandler
class KcmsFilesController(
    val pageFileRepository: PageFileRepository,
    val pageFilesService: PageFilesService,
) : CommonService() {

    class KcmsFilesRoute : GetRoute

    @RouteHandler
    fun files(route: KcmsFilesRoute): View {
        return KcmsFilesPage(
            pageFileRepository.findByPageId(null)
        )
    }

    data class KcmsFilesUploadRoute(
        val pageId: Long?,
        val fileUrl: String? = null,
        var files: ArrayList<MultipartFile>? = null,
    ) : MultipartPostRoute

    @RouteHandler
    fun uploadFiles(route: KcmsFilesUploadRoute): String {

        route.files?.forEach { file ->
            pageFilesService.save(route.pageId, file)
        }

        route.fileUrl.nullIfBlank()?.let { url ->
            pageFilesService.save(route.pageId, url)
        }

        return redirect(route.pageId?.let { KcmsPagesController.KcmsPageRoute(it) } ?: KcmsFilesRoute())
    }


    data class KcmsFileRemoveRoute(
        val pageId: Long?,
        val fileId: Long
    ) : PostRoute

    @RouteHandler
    fun removeFile(route: KcmsFileRemoveRoute): String {

        pageFilesService.removeFile(route.fileId)

        return redirect(route.pageId?.let { KcmsPagesController.KcmsPageRoute(it) } ?: KcmsFilesRoute())
    }

}