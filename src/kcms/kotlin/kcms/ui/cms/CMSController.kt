package kcms.ui.cms

import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Controller

@Controller
@RouteHandler
class CMSController {
    class CmsRoute : GetRoute

    @RouteHandler
    fun root(route: CmsRoute) = CMSDashboardPage()
}