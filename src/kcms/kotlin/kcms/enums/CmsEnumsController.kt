package kcms.enums

import kcms.common.CommonService
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.View

@Component
@RouteHandler
class CmsEnumsController(
    val enumValueService: EnumValueService,
    val enumValueRepository: EnumValueRepository
) : CommonService() {

    @RouteHandler
    fun categories(route: CmsEnumsCategoriesRoute): View {
        return CmsEnumsCategoriesPage()
    }

    @RouteHandler
    fun category(route: CmsEnumCategoryRoute): View {
        return CmsEnumCategoryPage(
            category = enumValueService.categories.first { it.id == route.c },
            values = enumValueRepository.findByCategory(route.c)
        )
    }

    @RouteHandler
    fun categorySave(route: CmsEnumCategorySaveRoute): String {
        transaction {
            route.values.forEach { (id, value) ->
                if(value.isNullOrBlank()) {
                    enumValueRepository.deleteById(id)
                } else {
                    enumValueRepository.save(
                        EnumValue(
                            id = id,
                            category = route.categoryId,
                            value = value
                        )
                    )
                }
            }
        }

        enumValueService.resetCaches()

        return redirect(CmsEnumsCategoriesRoute())
    }

    @RouteHandler
    fun newEnumValue(route: CmsEnumCategoryNewValueRoute): View {
        return CmsEnumCategoryPage.NewValueRowView(
            EnumValue(
                id = enumValueRepository.nextId(),
                category = route.categoryId,
                value = ""
            )
        )
    }
}