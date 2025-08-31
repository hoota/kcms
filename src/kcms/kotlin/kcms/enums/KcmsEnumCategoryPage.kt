package kcms.enums

import kcms.ui.KcmsGossRendererView
import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.WithOrdersRoute
import kcms.ui.cms.i18n.KcmsInternationalization
import kcms.ui.cms.orderChangeBlock
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute

data class KcmsEnumCategoryRoute(val c: String) : GetRoute

data class KcmsEnumCategorySaveRoute(
    val categoryId: String,
    val values: MutableMap<Long, String> = HashMap(),
    override val orders: MutableMap<Long, Int> = HashMap(),
    var multiValue: String? = null,
): PostRoute, WithOrdersRoute

class KcmsEnumCategoryPage(
    val category: KcmsEnumCategory,
    val values: List<EnumValue>
) : CommonKcmsPage(
    title = "${KcmsInternationalization.instance.enumCategory} // ${category.title}",
    module = MenuModule.ENUMS
) {

    override fun pageBody() {
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
                orderChangeBlock(route, v.id, v.order, false)
            }
        }
    }
}