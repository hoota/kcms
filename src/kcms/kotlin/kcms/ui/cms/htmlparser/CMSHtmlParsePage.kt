package kcms.ui.cms.htmlparser

import kcms.ui.cms.CommonCMSPage
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute

data class CmsHtmlParseRoute(
    val html: String? = null,
): GetRoute, PostRoute

class CMSHtmlParsePage(
    val route: CmsHtmlParseRoute,
    val code: String
) : CommonCMSPage(
    title = "HTML Parser"
) {

    override fun pageBody() {
        FORM(route) {
            DIV("form-group") {
                LABEL {
                    +"HTML"
                }
                TEXTAREA(route::html) {
                    classes("form-control")
                    style("width: 100%; height: 400px")
                }
            }
            SUBMIT("btn btn-primary", "Convert to Code")
        }

        PRE("mt-4") {
            +code
        }
    }
}