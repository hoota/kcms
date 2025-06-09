package kcms.jobs

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule

class KcmsJobsListPage(
    val jobsStatus: Map<String, String?>
) : CommonKcmsPage(
    title = "Executable Jobs",
    module = MenuModule.JOBS,
) {

    override fun pageBody() {
        TABLE("table no-top-border") {
            THEAD {
                TR {
                    TH("Job Name")
                    TH("Status")
                    TH("")
                }
            }
            TBODY {
                jobsStatus.entries.sortedBy { it.key }.forEach { j ->
                    TR {
                        TD { +j.key }
                        TD { +j.value }
                        TD {
                            FORM(KcmsJobsController.AdminJobRunRoute(j.key)) { route ->
                                HIDDEN(route::jobName)
                                SUBMIT("btn btn-sm btn-primary", "Run")
                            }
                        }
                    }
                }
            }
        }
    }
}