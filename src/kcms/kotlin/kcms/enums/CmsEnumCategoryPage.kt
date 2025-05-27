package kcms.enums

import kcms.ui.KCMSGossRendererView
import kcms.ui.cms.CommonCMSPage
import kcms.ui.cms.MenuModule
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute

data class CmsEnumCategoryRoute(val c: String) : GetRoute

data class CmsEnumCategorySaveRoute(
    val categoryId: String,
    val values: MutableMap<Long, String> = HashMap()
): GetRoute

data class CmsEnumCategoryNewValueRoute(val categoryId: String) : PostRoute

class CmsEnumCategoryPage(
    val category: CmsEnumCategory,
    val values: List<EnumValue>
) : CommonCMSPage(
    title = "Enum Category // ${category.title}",
    module = MenuModule.ENUMS
) {

    val block = NewValueRowView()

    override fun preTitle() {
        FORM(CmsEnumCategoryNewValueRoute(categoryId = category.id)) {
            style("float: right;")
            ajaxForm()

            HIDDEN(it::categoryId)

            BUTTON("btn btn-primary") {
                +"New Value"
            }
        }
    }

    override fun pageBody() {
        FORM(CmsEnumCategorySaveRoute(categoryId = category.id)) { route ->
            HIDDEN(route::categoryId)
            TABLE("table") {
                THEAD {
                    TR {
                        TH {
                            SUBMIT("btn btn-success", "Save") {
                                style("float: right;")
                            }
                            +"Value"
                        }
                    }
                }
                THEAD {
                    block.newValueTr()

                    values.forEach { v ->
                        block.valueTr(route, v)
                    }
                }
            }
            SUBMIT("btn btn-success", "Save")
        }
    }

    class NewValueRowView(
        val v: EnumValue? = null
    ) : KCMSGossRendererView() {
        override fun draw() {
            newValueTr()
            valueTr(CmsEnumCategorySaveRoute(categoryId = v!!.category), v)
        }

        fun newValueTr() {
            TR {
                id("new-value")
            }
        }

        fun valueTr(route: CmsEnumCategorySaveRoute, v: EnumValue) {
            TR {
                TD {
                    namePrefix(route::values) {
                        INPUT("form-control") {
                            name(v.id.toString())
                            type("text")
                            value(v.value)
                        }
                    }
                }
            }
        }
    }
}