package kcms.enums

import kcms.common.CommonService
import kcms.common.nullIfBlank
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.View

@Component
@RouteHandler
class KcmsEnumsController(
    val enumValueService: EnumValueService,
    val enumValueRepository: EnumValueRepository
) : CommonService() {

    @RouteHandler
    fun categories(route: KcmsEnumsCategoriesRoute): View {
        return KcmsEnumsCategoriesPage()
    }

    @RouteHandler
    fun category(route: KcmsEnumCategoryRoute): View {
        return KcmsEnumCategoryPage(
            category = enumValueService.categories.first { it.id == route.c },
            values = enumValueRepository.findByCategory(route.c)
        )
    }

    @RouteHandler
    fun categorySave(route: KcmsEnumCategorySaveRoute): String {
        transaction {
            route.values.forEach { (id, value) ->
                if(value.isNullOrBlank()) {
                    enumValueRepository.deleteById(id)
                } else {
                    enumValueRepository.save(
                        EnumValue(
                            id = id,
                            category = route.categoryId,
                            value = value,
                            order = route.orders[id] ?: 0
                        )
                    )
                }
            }
            val maxOrder = route.orders.values.maxOrNull() ?: 0
            route.multiValue?.split("\n")?.forEachIndexed { index, line ->
                line.nullIfBlank()?.let { v ->
                    enumValueRepository.save(
                        EnumValue(
                            id = enumValueRepository.nextId(),
                            category = route.categoryId,
                            value = v,
                            order = maxOrder + index
                        )
                    )
                }
            }
        }

        enumValueService.resetCaches()

        return redirect(KcmsEnumsCategoriesRoute())
    }

}