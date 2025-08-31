package kcms.ui.cms

import kcms.ui.KcmsGossRenderer
import kiss.gossr.spring.GetRoute
import kotlin.math.absoluteValue

data class PagedData<T>(
    val data: List<T>,
    val currentPage: Int,
    val lastPage: Int,
    val pageSize: Int,
    val total: Int,
) {
    companion object {
        fun <T> of(list: List<T>, page:Int, pageSize: Int): PagedData<T> = PagedData(
            data = list.drop( (page-1) * pageSize).take(pageSize),
            currentPage = page,
            lastPage = (list.size + pageSize - 1) / pageSize,
            pageSize = pageSize,
            total = list.size
        )

        fun <T, R> of(sequence: Sequence<T>, page:Int, pageSize: Int, enricher: (List<T>) -> List<R>): PagedData<R> {
            var total = 0
            val data = ArrayList<T>()
            val minIndex = (page - 1)*pageSize
            val maxIndex = minIndex + pageSize

            sequence.forEachIndexed { index, t ->
                total ++
                if(minIndex <= index && index < maxIndex) data.add(t)
            }

            return PagedData(
                data = enricher(data),
                currentPage = page,
                lastPage = (total + pageSize - 1) / pageSize,
                pageSize = pageSize,
                total = total
            )
        }
    }
}

fun <T> Sequence<T>.paged(page:Int, pageSize: Int) = PagedData.of(this, page, pageSize) { it }
fun <T, R> Sequence<T>.paged(page:Int, pageSize: Int, enricher: (List<T>) -> List<R>) = PagedData.of(this, page, pageSize, enricher)

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