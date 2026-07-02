package kcms.ui

import java.time.LocalDateTime

class ErrorPage(
    val description: String? = null,
    val e: Exception? = null
) : KcmsGossRendererView() {

    override fun draw() {
        noEscape("<!DOCTYPE html>")
        HTML {
            attr("lang", "en")
            HEAD {
                EL("meta") { attr("charset", "utf-8") }
                EL("meta") {
                    name("viewport")
                    attr("content", "width=device-width, initial-scale=1, shrink-to-fit=no")
                }

                LINK(
                    rel = "stylesheet",
                    href = "https://cdn.jsdelivr.net/npm/bootstrap@4.4.1/dist/css/bootstrap.min.css"
                )
                EL("title") {
                    +description
                }
            }
            BODY {
                DIV("mt-4 mb-4") {
                    +formatDateTime(LocalDateTime.now())
                    BR()
                    +"Dev team is already notified about this error."
                }
                PRE {
                    +description
                    +e?.stackTraceToString()
                }
            }
        }
    }
}