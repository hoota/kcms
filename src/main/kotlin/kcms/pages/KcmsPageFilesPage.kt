package kcms.pages

import kcms.files.KcmsFilesListBlock
import kcms.files.KcmsImageScale
import kcms.files.KcmsImageScaleType
import kcms.files.PageFile
import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization
import org.springframework.stereotype.Component

@Component
data class KcmsImageScaleH100(
    override val size: Int = 100,
    override val type: KcmsImageScaleType = KcmsImageScaleType.HEIGHT
) : KcmsImageScale

@Component
data class KcmsImageScaleW100(
    override val size: Int = 100,
    override val type: KcmsImageScaleType = KcmsImageScaleType.WIDTH
) : KcmsImageScale

class KcmsPageFilesPage(
    val p: Page,
    val files: List<PageFile>,
    val noChildren: Boolean
) : CommonKcmsPage(
    title = "${KcmsInternationalization.instance.page} #${p.id} // ${p.title}",
    module = MenuModule.PAGES,
    showTitleAsHeader = false
) {

    private fun drawTabs() {
        DIV("nav nav-tabs mt-1 mb-1") {
            A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.PROPERTIES))
                +i18n.properties
            }
            A("nav-item nav-link active show") {
                href("#")
                +i18n.files
            }
            if(!noChildren) A("nav-item nav-link") {
                href(KcmsPageRoute(id = p.id, tab = KcmsPageTabs.CHILDREN))
                +i18n.children
            }
        }
    }


    override fun pageBody() {
        H3 {
            classes("mt-3 page-title")
            A {
                href(p.slug)
                +"${i18n.page} #${p.id}"
            }
            +" // ${p.title}"
        }

        drawTabs()

        KcmsFilesListBlock(
            pageId = p.id,
            files = files
        ).draw()
    }
}