package kcms.enums

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kiss.gossr.spring.GetRoute

class KcmsEnumsCategoriesRoute : GetRoute

class KcmsEnumsCategoriesPage : CommonKcmsPage(
    title = "Enum Categories",
    module = MenuModule.ENUMS
) {
    override fun pageBody() {
        TABLE("table") {
            THEAD {
                TR {
                    TH("Category")
                }
            }
            TBODY {
                EnumValueService.instance.categories.forEach { c ->
                    TR {
                        TD {
                            A {
                                href(KcmsEnumCategoryRoute(c.id))
                                +c.title
                            }
                        }
                    }
                }
            }
        }
    }
}