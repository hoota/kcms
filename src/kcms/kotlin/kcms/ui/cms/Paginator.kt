package kcms.ui.cms

import kcms.ui.KcmsGossRenderer
import kiss.gossr.spring.GetRoute
import kotlin.math.absoluteValue

data class PagedData<T>(
    val data: List<T>,
    val currentPage: Int,
    val lastPage: Int,
    val pageSize: Int,
) {
    companion object {
        fun <T> of(list: List<T>, page:Int, pageSize: Int): PagedData<T> = PagedData(
            data = list.drop( (page-1) * pageSize).take(pageSize),
            currentPage = page,
            lastPage = (list.size + pageSize - 1) / pageSize,
            pageSize = pageSize
        )
    }
}

class Paginator : KcmsGossRenderer() {
    fun <T> draw(data: PagedData<T>, routeBuilder: (Int) -> GetRoute) {
        if(data.lastPage <= 1) return
        NAV {
            UL("pagination mb-0") {
                var lastP = 1
                (1..data.lastPage).forEach { p ->
                    if(p <= 3 || (p - data.currentPage).absoluteValue <= 2 || p >= data.lastPage - 2) {
                        if(p > lastP + 1) {
                            LI("page-item disabled text-secondary ml-2 mr-2") {
                                +"..."
                            }
                        }

                        LI("page-item") {
                            if(data.currentPage == p) classes("active")
                            A("page-link") {
                                href(routeBuilder(p))
                                +p.toString()
                            }
                        }

                        lastP = p
                    }
                }
            }
        }
    }
}