package kcms.files

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization

class KcmsFilesPage(
    val files: List<PageFile>
) : CommonKcmsPage(
    title = KcmsInternationalization.instance.sharedFiles,
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