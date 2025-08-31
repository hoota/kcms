package kcms.widgets

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

interface WidgetComponent {
    val title: String
}

@Service
class WidgetComponentService(
    private val applicationContext: ApplicationContext,
    val sitePropertiesDescriptors: List<SitePropertiesDescriptor>,
) {

    fun getWidgetComponents(clazz: Class<out WidgetComponent>): Map<String, WidgetComponent> {
        return applicationContext.getBeansOfType(clazz)
    }

    @PostConstruct
    fun postConstruct() {
        instance = this
    }

    fun getByBeanName(beanName: String): WidgetComponent? {
        return applicationContext.getBean(beanName) as? WidgetComponent
    }

    companion object {
        lateinit var instance: WidgetComponentService
    }
}