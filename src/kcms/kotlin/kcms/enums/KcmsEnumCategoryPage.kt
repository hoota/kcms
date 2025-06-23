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
): PostRoute

data class KcmsEnumCategoryNewValueRoute(val categoryId: String) : PostRoute

class KcmsEnumCategoryPage(
    val category: KcmsEnumCategory,
    val values: List<EnumValue>
) : CommonKcmsPage(
    title = "${KcmsInternationalization.instance.enumCategory} // ${category.title}",
    module = MenuModule.ENUMS
) {

    val block = NewValueRowView()

    override fun preTitle() {
        FORM(KcmsEnumCategoryNewValueRoute(categoryId = category.id)) {
            style("float: right;")
            ajaxForm()

            HIDDEN(it::categoryId)

            BUTTON("btn btn-primary") {
                +i18n.newValue
            }
        }
    }

    override fun pageBody() {
        STYLE("""
            tr:nth-of-type(2) span.move-value-up { display: none; }          
            tr:last-of-type span.move-value-down { display: none; }          
        """)

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
                    block.newValueTr()

                    values.sortedBy { it.order }.forEach { v ->
                        block.valueTr(route, v)
                    }
                }
            }
            SUBMIT("btn btn-success", i18n.save)
        }
    }

    class NewValueRowView(
        val v: EnumValue? = null
    ) : KcmsGossRendererView() {
        override fun draw() {
            newValueTr()
            valueTr(KcmsEnumCategorySaveRoute(categoryId = v!!.category), v)
        }

        fun newValueTr() {
            TR {
                id("new-value")
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
}