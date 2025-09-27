package kcms.enums

import kcms.common.Caches
import kcms.common.CommonService
import kcms.common.nullIfBlank
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import kotlin.jvm.optionals.getOrNull

@Component
@RouteHandler
class KcmsEnumsController(
    val enumCategoryRepository: EnumCategoryRepository,
    val enumValueService: EnumValueService,
    val enumValueRepository: EnumValueRepository
) : CommonService() {

    data class EnumCategorySaveRoute(
        val id: String,
        val title: String,
    ) : PostRoute

    @RouteHandler
    fun enumCategorySave(route: EnumCategorySaveRoute): ModelAndView {
        return try {
            transaction {
                val c = enumCategoryRepository.findById(route.id).getOrNull()
                    ?: EnumCategory(route.id, route.title)
                c.title = route.title
                enumCategoryRepository.save(c)
            }

            Caches.instance.resetAll()

            ModelAndView(
                redirectToReferer(KcmsEnumsCategoriesRoute())
            )
        }catch(e: Exception) {
            ModelAndView(KcmsEnumCategoryEditModal(
                EnumCategory("", ""),
                errorMessage = e.message
            ))
        }
    }

    class EnumCategoryEditRoute(val id: String) : GetRoute

    @RouteHandler
    fun enumCategoryEdit(route: EnumCategoryEditRoute): View {
        return KcmsEnumCategoryEditModal(
            enumCategoryRepository.findById(route.id).get()
        )
    }

    data class EnumCategoryAddRoute(
        val id: String? = null,
        val title: String? = null,
    ) : GetRoute

    @RouteHandler
    fun enumCategoryAdd(route: EnumCategoryAddRoute): View {
        return KcmsEnumCategoryEditModal(
            EnumCategory(route.id ?: "", route.title ?: "")
        )
    }

    @RouteHandler
    fun categories(route: KcmsEnumsCategoriesRoute): View {
        return KcmsEnumsCategoriesPage(enumValueService.getCategoriesList())
    }

    @RouteHandler
    fun category(route: KcmsEnumCategoryRoute): View {
        return KcmsEnumCategoryPage(
            category = enumValueService.getCategoriesList().first { it.id == route.c },
            values = enumValueRepository.findByCategory(route.c)
        )
    }

    @RouteHandler
    fun categorySave(route: KcmsEnumCategorySaveItemsRoute): String {
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