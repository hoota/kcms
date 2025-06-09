package kcms.ui.cms.htmlparser

import kcms.common.nullIfBlank
import kiss.gossr.spring.RouteHandler
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.springframework.stereotype.Component
import org.springframework.web.servlet.View

@Component
@RouteHandler
class KcmsHtmlParserController {
    @RouteHandler
    fun parseHtml(route: KcmsHtmlParseRoute): View {
        val code = StringBuilder()

        Jsoup.parse(route.html ?: "")?.body()?.let {
            convertToCode(code, it)
        }

        return KcmsHtmlParsePage(
            route,
            code.toString()
        )
    }

    private fun convertToCode(code: StringBuilder, n: Element, prefix: String = " ") {
        if(n.tagName().uppercase() == "SVG") {
            code.append(prefix).append("noEscape(\"\"\"${n.toString()}\"\"\")\n")
            return
        }
        val classes = n.attr("class").nullIfBlank()
        if(classes == null) {
            code.append(prefix).append(n.tagName().uppercase()).append(" {\n")
        } else {
            code.append(prefix).append(n.tagName().uppercase()).append("(").append('"').append(classes).append('"').append(") {\n")
        }

        n.attributes().filter { it.key != "class" }.forEach { a ->
            val key = a.key.lowercase()
            if(key.startsWith("data-")) {
                code.append("$prefix ").append("""data("$key", "${a.value}")""").append("\n")
            } else {
                code.append("$prefix ").append("""$key("${a.value}")""").append("\n")
            }
        }

        n.childNodes().forEach { c ->
            if(c is Element) convertToCode(code, c, "$prefix ")
            if(c is TextNode) {
                c.text().nullIfBlank()?.let {
                    code.append("$prefix ").append("""+"$it"""").append("\n")
                }
            }
        }
        code.append(prefix).append("}\n")
    }
}