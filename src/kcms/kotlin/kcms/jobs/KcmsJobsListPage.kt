package kcms.jobs

import kcms.ui.cms.CommonKcmsPage
import kcms.ui.cms.MenuModule
import kcms.ui.cms.i18n.KcmsInternationalization

class KcmsJobsListPage(
    val jobsStatus: Map<String, String?>
) : CommonKcmsPage(
    title = KcmsInternationalization.instance.backgroundJobs,
    module = MenuModule.JOBS,
) {

    override fun pageBody() {
        TABLE("table no-top-border") {
            THEAD {
                TR {
                    TH(i18n.jobName)
                    TH(i18n.status)
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
                                SUBMIT("btn btn-sm btn-primary", i18n.runJob)
                            }
                        }
                    }
                }
            }
        }
    }
}