package kcms.jobs

import kcms.common.CommonService
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Service
class JobsRunnerService(
    val jobs: List<ExecutableJob>
) : CommonService() {
    private val runnings = ConcurrentHashMap<String, String>()

    fun jobsStatus(): Map<String, String?> = jobs.map {
        it.javaClass.simpleName
    }.associateWith {
        runnings[it]
    }

    fun runJob(name: String) {
        jobs.firstOrNull { it.javaClass.simpleName == name }?.let { job ->
            synchronized(job) {
                try {
                    runnings[name] = "Running..."
                    job.run()
                    runnings.remove(name)
                }catch(e: Exception) {
                    log.error("runJob($name) error : ${e.message}", e)
                    runnings[name] = e.message ?: e.javaClass.simpleName
                }
            }
        }
    }

    @PostConstruct
    fun postContruct() {
        instance = this
    }

    companion object {
        lateinit var instance: JobsRunnerService
    }
}

interface ExecutableJob {
    fun run()
}