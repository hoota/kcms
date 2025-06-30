package kcms.enums

import kcms.ui.KcmsGossRendererView
import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute

data class KcmsEnumCategoryRoute(val c: String) : GetRoute

data class KcmsEnumCategorySaveRoute(
    val categoryId: String,
    val values: MutableMap<Long, String> = HashMap(),
    val orders: MutableMap<Long, Int> = HashMap(),
    var multiValue: String? = null,
): PostRoute

class KcmsEnumCategoryPage(
    val category: KcmsEnumCategory,
    val values: List<EnumValue>
) : CommonKcmsPage(
    title = "${KcmsInternationalization.instance.enumCategory} // ${category.title}",
    module = MenuModule.ENUMS
) {

    override fun pageBody() {
        STYLE(
            """
            tr:first-of-type span.move-value-up { display: none; }          
            tr:last-of-type span.move-value-down { display: none; }          
        """
        )

        FORM(KcmsEnumCategorySaveRoute(categoryId = category.id)) { route ->
            HIDDEN(route::categoryId)
            TABLE("table") {
                THEAD {
                    TR {
                        TH(i18n.value)
                        TH {
                            SUBMIT("btn btn-success", i18n.save) {
                                style("float: right;")
                            }
                        }
                    }
                }
                THEAD {
                    values.sortedBy { it.order }.forEach { v ->
                        valueTr(route, v)
                    }
                }
            }
            TEXTAREA(route::multiValue) {
                classes("form-control mb-2")
                placeholder(i18n.newValues)
                style("width: 100%; height: 200px")
            }
            SUBMIT("btn btn-success", i18n.save)
        }
    }

    fun valueTr(route: KcmsEnumCategorySaveRoute, v: EnumValue) {
        TR("enum-value") {
            TD {
                namePrefix(route::values) {
                    INPUT("form-control") {
                        name(v.id.toString())
                        type("text")
                        value(v.value)
                    }
                }
            }
            TD {
                namePrefix(route::orders) {
                    INPUT("order") {
                        name(v.id.toString())
                        type("hidden")
                        value(v.order)
                    }
                }
                SPAN("btn btn-link move-value-up") {
                    style("border: none; padding: 0 0;")
                    title(i18n.moveToTop)
                    onClick("moveRowOnTop(this)")
                    +"⤒"
                }
                SPAN("btn btn-link move-value-up") {
                    style("border: none; padding: 0 0;")
                    title(i18n.moveUp)
                    onClick("moveRowUp(this)")
                    +"↑"
                }
                SPAN("btn btn-link move-value-down") {
                    style("border: none; padding: 0 0;")
                    title(i18n.moveDown)
                    onClick("moveRowDown(this)")
                    +"↓"
                }
                SPAN("btn btn-link move-value-down") {
                    style("border: none; padding: 0 0;")
                    title(i18n.moveToTheBottom)
                    onClick("moveRowToBottom(this)")
                    +"⤓"
                }
            }
        }
    }
}