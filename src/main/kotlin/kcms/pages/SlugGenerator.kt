package kcms.pages

import kcms.widgets.WidgetRenderContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

interface SlugGenerator {
    val templateId: String
    fun generateSlug(
        page: Page,
        properties: WidgetRenderContext
    ): String
}

@Component
class SlugGenerators(
    generators: List<SlugGenerator>
){
    val generators = generators.associateBy { it.templateId }

    @PostConstruct
    fun postConstruct() {
        instance = this
    }

    companion object {
        lateinit var instance : SlugGenerators
    }
}