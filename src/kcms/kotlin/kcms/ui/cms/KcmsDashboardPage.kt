package kcms.ui.cms

import kiss.gossr.spring.GetRoute
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

class KcmsDashboardPage : CommonKcmsPage(
    title = "K*CMS",
    showTitleAsHeader = false
) {

    override fun pageBody() {
        KcmsDashboardComponents.instance.components.forEach {
            it.render()
        }
    }
}

interface KcmsDashboardComponent {
    fun render()
}

interface KcmsMenuOption {
    fun dropdown(): Pair<String, List<Pair<String, GetRoute>>>
}

@Component
class KcmsDashboardComponents(
    val components: List<KcmsDashboardComponent>,
    val menuOptions: List<KcmsMenuOption>
) {

    @PostConstruct
    fun postConstruct() {
        instance = this
    }

    companion object {
        lateinit var instance: KcmsDashboardComponents
    }
}