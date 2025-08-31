package kcms.ui.cms

import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.View
import java.util.concurrent.TimeUnit
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Controller
@RouteHandler
class KcmsController {
    class KcmsRoute : GetRoute

    @RouteHandler
    fun root(
        response: HttpServletResponse,
        route: KcmsRoute
    ): View {
        response.addCookie(Cookie(ADMIN_COOKIE_NAME, "yes").also {
            it.path = "/"
            it.maxAge = TimeUnit.DAYS.toSeconds(1).toInt()
        })
        return KcmsDashboardPage()
    }

    companion object {
        val ADMIN_COOKIE_NAME = "kcms_admin"
    }
}