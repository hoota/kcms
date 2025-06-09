package kcms.ui.cms

import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Controller

@Controller
@RouteHandler
class KcmsController {
    class KcmsRoute : GetRoute

    @RouteHandler
    fun root(route: KcmsRoute) = KcmsDashboardPage()
}