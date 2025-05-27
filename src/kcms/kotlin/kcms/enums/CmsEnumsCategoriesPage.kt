package kcms.enums

import kcms.ui.cms.CommonCMSPage
import kcms.ui.cms.MenuModule
import kiss.gossr.spring.GetRoute

class CmsEnumsCategoriesRoute : GetRoute

class CmsEnumsCategoriesPage : CommonCMSPage(
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
                                href(CmsEnumCategoryRoute(c.id))
                                +c.title
                            }
                        }
                    }
                }
            }
        }
    }
}