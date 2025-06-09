package kcms.files

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule

class KcmsFilesPage(
    val files: List<PageFile>
) : CommonKcmsPage(
    title = "Shared Files",
    module = MenuModule.FILES,
    showTitleAsHeader = false
) {
    override fun pageBody() {
        KcmsFilesListBlock(
            pageId = null,
            files = files
        ).draw {
            H3 {
                classes("mt-3 page-title")
                +title
            }
        }
    }
}