package kcms.common

import com.fasterxml.jackson.databind.ObjectMapper
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.GossSpringRenderer
import kiss.gossr.spring.RoutesHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import javax.persistence.EntityManager

open class CommonService : Loggable {
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    lateinit var entityManager: EntityManager

    fun <T> transaction(function: (em: EntityManager) -> T): T {
        return TransactionTemplate(transactionManager).execute { function(entityManager) } as T
    }

    fun redirect(url: String) = "redirect:$url"
    fun redirect(route: GetRoute) = redirect(RoutesHelper.buildRouteUri(route))

    fun redirectToReferer(default: String = "/") =
        redirect((GossSpringRenderer.request()?.getHeader("referer") ?: default))

    fun redirectToReferer(default: GetRoute) =
        redirectToReferer(RoutesHelper.buildRouteUri(default))
}