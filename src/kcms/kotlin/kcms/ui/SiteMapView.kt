package kcms.ui

import kcms.pages.Page
import kiss.gossr.GossRenderer
import kiss.gossr.spring.GossSpringRenderer
import org.springframework.web.servlet.View
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SiteMapView(
    val urlBase: String,
    val pages: List<Page>
) : KcmsGossRenderer(), View {
    override fun render(
        params: MutableMap<String, *>?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {

        response.also {
            it.contentType = "text/xml"
        }.outputStream.writer(Charsets.UTF_8).buffered(1 shl 15).use { out ->
            GossRenderer.use(
                out = out,
                dateTimeFormats = GossSpringRenderer.getDateTimeFormats(),
                moneyFormats = GossSpringRenderer.getMoneyFormats(),
                renderFunction = this::draw
            )
        }
    }

    fun draw() {
        EL("urlset") {
            attr("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9")
            attr("xmlns:xhtml", "http://www.w3.org/1999/xhtml")

            pages.forEach { p ->
                EL("url") {
                    EL("loc") {
                        +"$urlBase${p.slug}"
                    }
                }
            }
        }
    }
}