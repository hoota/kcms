package kcms.jobs

import kcms.common.CommonService
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.PostRoute
import kiss.gossr.spring.RouteHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.View
import java.util.concurrent.ForkJoinPool

@Component
@RouteHandler
class KcmsJobsController(
    val jobsRunnerService: JobsRunnerService
) : CommonService() {
    class AdminJobsListRoute : GetRoute

    @RouteHandler
    fun jobsList(route: AdminJobsListRoute): View {
        return KcmsJobsListPage(
            jobsRunnerService.jobsStatus()
        )
    }

    data class AdminJobRunRoute(val jobName: String) : PostRoute

    @RouteHandler
    fun runJob(
        route: AdminJobRunRoute
    ): String {

        ForkJoinPool.commonPool().submit {
            jobsRunnerService.runJob(route.jobName)
        }

        return redirectToReferer(AdminJobsListRoute())
    }
}