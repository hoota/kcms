package kcms.pages

import kcms.enums.EnumValueService
import kcms.ui.KCMSGossRenderer
import kcms.widgets.WidgetPropertyDescriptor
import kcms.widgets.WidgetPropertyType

class CmsPropertiesEditBlock : KCMSGossRenderer() {
    fun draw(
        values: Map<String, Map<String, PageProperty>>,
        widgetId: String,
        properties: List<WidgetPropertyDescriptor>
    ) {
        properties.forEach { p ->
            val v = values.get(widgetId)?.get(p.key)
            DIV("form-group") {
                LABEL {
                    +"${p.title} (${p.type})"
                }
                when(p.type) {
                    WidgetPropertyType.HTML -> TEXTAREA("form-control") {
                        style("width: 100%; height: 200px")
                        name(p.key)
                        +v?.text
                    }

                    WidgetPropertyType.TEXT -> INPUT("form-control") {
                        name(p.key)
                        type("text")
                        value(v?.text)
                    }

                    WidgetPropertyType.DATE -> INPUT("form-control") {
                        name(p.key)
                        type("date")
                        value(v?.date)
                    }
                    WidgetPropertyType.NUMBER -> INPUT("form-control") {
                        name(p.key)
                        type("number")
                        value(v?.number)
                    }
                    WidgetPropertyType.ENUM -> SELECT("form-control") {
                        name(p.key)
                        EnumValueService.instance.getEnumValues(p.enumCategory ?: "").forEach { e ->
                            OPTION(e.id, e.value, selected = v?.number == e.id)
                        }
                    }
                }
            }
        }
    }
}