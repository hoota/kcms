package kcms.ui.cms

import kcms.ui.KCMSGossRendererView

enum class ModalSize(val cssClass: String) {
    small("modal-sm"),
    medium(""),
    large("modal-lg"),
    xlarge("modal-xl"),
}

abstract class CommonModal(
    val title: String,
    private val size: ModalSize = ModalSize.xlarge
): KCMSGossRendererView() {

    open fun buttonTitle(): String? = null
    open fun title() {
        + title
    }
    protected abstract fun body()

    override fun draw() = modal()

    private fun modal() {
        DIV("modal fade") {
            attr("tabindex", "-1")
            attr("role", "dialog")
            id("modal-${this.javaClass.simpleName}")
            attr("aria-hidden", "true")
            DIV("modal-dialog modal-dialog-centered ${size.cssClass}") {
                DIV("modal-content") {
                    DIV("modal-content") {
                        DIV("modal-header") {
                            H5 {
                                classes("modal-title")
                                title()
                            }
                            BUTTON {
                                type("button")
                                classes("close")
                                attr("data-dismiss", "modal")
                                attr("aria-label", "Close")
                                SPAN {
                                    attr("aria-hidden", "true")
                                    +"×"
                                }
                            }
                        }

                        try {
                            body()
                        }catch(e: Exception) {
                            DIV("alert alert-danger") {
                                PRE {
                                    +e.stackTraceToString()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}