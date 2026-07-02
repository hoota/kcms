package kcms.enums

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization
import kiss.gossr.spring.GetRoute

class KcmsEnumsCategoriesRoute : GetRoute

class KcmsEnumsCategoriesPage(
    val categories: List<KcmsEnumCategory>
) : CommonKcmsPage(
    title = KcmsInternationalization.instance.enumCategories,
    module = MenuModule.ENUMS
) {
    override fun preTitle() {
        BUTTON("btn btn-primary") {
            style("float: right;")
            openModalOnClick(KcmsEnumsController.EnumCategoryAddRoute())
            +i18n.addCategory
        }
    }

    override fun pageBody() {
        TABLE("table") {
            THEAD {
                TR {
                    TH(i18n.category)
                    TH()
                }
            }
            TBODY {
                categories.forEach { c ->
                    TR {
                        TD {
                            A {
                                href(KcmsEnumCategoryRoute(c.id))
                                +c.title
                            }
                        }
                        TD {
                            if(c is EnumCategory) {
                                BUTTON("btn btn-sm btn-primary") {
                                    openModalOnClick(KcmsEnumsController.EnumCategoryEditRoute(c.id))
                                    +i18n.edit
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}