package kcms.enums

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization
import kiss.gossr.spring.GetRoute

class KcmsEnumsCategoriesRoute : GetRoute

class KcmsEnumsCategoriesPage : CommonKcmsPage(
    title = KcmsInternationalization.instance.enumCategories,
    module = MenuModule.ENUMS
) {
    override fun pageBody() {
        TABLE("table") {
            THEAD {
                TR {
                    TH(i18n.category)
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