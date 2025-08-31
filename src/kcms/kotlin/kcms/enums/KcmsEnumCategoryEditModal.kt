package kcms.enums

import kcms.common.isNotNullNorBlank
import kcms.ui.cms.CommonModal
import kcms.ui.cms.ModalSize
import kcms.ui.cms.i18n.KcmsInternationalization

class KcmsEnumCategoryEditModal(
    val c: EnumCategory,
    val errorMessage: String? = null,
) : CommonModal(
    title = KcmsInternationalization.instance.enumCategory,
    size = ModalSize.large
) {

    override fun body() {
        FORM(KcmsEnumsController.EnumCategorySaveRoute(id = c.id, title = c.title)) { route ->
            ajaxForm()
            classes("modal-form")

            DIV("modal-body") {
                showErrorMessage(errorMessage)

                DIV("form-group row") {
                    LABEL("col-4 col-form-label") {
                        +"ID:"
                    }
                    DIV("col-6") {
                        INPUT("form-control") {
                            required(true)
                            readonly(c.id.isNotNullNorBlank())
                            nameValueString(route::id)
                        }
                    }
                }
                DIV("form-group row") {
                    LABEL("col-4 col-form-label") {
                        +i18n.category
                    }
                    DIV("col-6") {
                        INPUT("form-control") {
                            required(true)
                            nameValueString(route::title)
                        }
                    }
                }
            }

            DIV("modal-footer") {
                BUTTON {
                    type("button")
                    classes("btn btn-secondary")
                    attr("data-dismiss", "modal")
                    +i18n.close
                }

                INPUT("btn btn-success modal-submit-btn") {
                    type("submit")
                    value(i18n.save)
                }
            }
        }

    }
}