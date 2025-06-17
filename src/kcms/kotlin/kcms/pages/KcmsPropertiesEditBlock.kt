package kcms.pages

import kcms.enums.EnumValueService
import kcms.ui.KcmsGossRenderer
import kcms.widgets.WidgetComponentService
import kcms.widgets.WidgetPropertyDescriptor
import kcms.widgets.WidgetPropertyType
import java.util.*

class KcmsPropertiesEditBlock(
    val route: WidgetPropertiesSaveRoute,
    val values: Map<String, PageProperty>
) : KcmsGossRenderer() {

    fun draw(
        properties: List<WidgetPropertyDescriptor>
    ) {
        properties.forEach { p ->
            val v = values.get(p.key)
            DIV("form-group") {
                LABEL("font-weight-bold") {
                    +p.title
                }
                when(p.type) {
                    WidgetPropertyType.TEXT -> TEXTAREA("form-control") {
                        style("width: 100%; height: 200px")
                        name(p.key)
                        required(p.required)
                        +v?.text
                    }

                    WidgetPropertyType.STRING -> INPUT("form-control") {
                        name(p.key)
                        type("text")
                        required(p.required)
                        value(v?.text)
                    }

                    WidgetPropertyType.DATE -> INPUT("form-control") {
                        name(p.key)
                        type("date")
                        required(p.required)
                        value(v?.date)
                    }

                    WidgetPropertyType.NUMBER -> INPUT("form-control") {
                        name(p.key)
                        type("number")
                        required(p.required)
                        value(v?.number)
                        min(p.numberMin?.toString())
                        step(p.numberStep?.toString())
                    }

                    WidgetPropertyType.ENUM -> SELECT("form-control") {
                        name(p.key)
                        if(!p.required) OPTION("--")
                        EnumValueService.instance.getEnumValues(p.enumCategory).forEach { e ->
                            OPTION(e.id, e.value, selected = v?.number?.toLong() == e.id)
                        }
                    }

                    WidgetPropertyType.ENUMS_SET ->
                        drawEnumsSetInput(p, v?.asList?.mapNotNull { it.toLongOrNull() } ?: emptyList())

                    WidgetPropertyType.WIDGET_COMPONENT -> SELECT("form-control") {
                        name(p.key)
                        if(!p.required) OPTION("--")
                        p.widgetComponentClass?.let { clazz ->
                            WidgetComponentService.instance.getWidgetComponents(clazz).forEach { wc ->
                                OPTION(wc.key, wc.value.title, selected = v?.text == wc.key)
                            }
                        }
                    }

                    WidgetPropertyType.LIST -> drawListInput(p, v?.asList)

                    WidgetPropertyType.MAP -> drawEnumMapInput(p, v?.asMap)
                }
            }
        }
    }

    private fun drawEnumsSetInput(
        p: WidgetPropertyDescriptor,
        values: List<Long>
    ) = DIV("row") {
        val enumValues = EnumValueService.instance.getEnumValues(p.enumCategory)
        (0 until p.columns).forEach { c ->
            DIV("col-12 col-md") {
                enumValues.forEachIndexed { index, e ->
                    if(index % p.columns == c) DIV {
                        val cbId = "checkbox-${UUID.randomUUID()}"
                        INPUT {
                            type("checkbox")
                            id(cbId)
                            name(p.key)
                            value(e.id)
                            checked(e.id in values)
                        }
                        LABEL {
                            forAttr(cbId)
                            +e.value
                        }
                    }
                }
            }
        }
    }

    private fun drawEnumMapInput(
        p: WidgetPropertyDescriptor,
        values: Map<Long, String>?,
    ) = DIV("row") {
        val enumValues = EnumValueService.instance.getEnumValues(p.enumCategory)
        (0 until p.columns).forEach { c ->
            DIV("col-12 col-md") {
                enumValues.forEachIndexed { index, e ->
                    if(index % p.columns == c) DIV("form-group") {
                        LABEL {
                            +e.value
                        }
                        INPUT("form-control") {
                            name("${p.key}@${e.id}")
                            type("text")
                            value(values?.get(e.id))
                        }
                    }
                }
            }
        }
    }

    private fun drawListInput(
        p: WidgetPropertyDescriptor,
        values: List<String>?,
    ) = DIV("row") {
        (0 until p.columns).forEach { c ->
            DIV("col-12 col-md") {
                UL("ul pl-3") {
                    values?.filterIndexed { index, _ -> index % p.columns == c }?.forEach { v ->
                        LI("mb-1") {
                            INPUT("form-control") {
                                name(p.key)
                                type("text")
                                value(v)
                            }
                        }
                    }

                    LI("d-none mb-1") {
                        INPUT("form-control") {
                            name(p.key)
                            type("text")
                        }
                    }
                    if(c == 0) LI {
                        SPAN("btn btn-sm btn-outline-primary") {
                            onClick(
                                """
const ul = $(this).closest('.ul');
const hiddenLi = ul.find('li.d-none').first();
if (hiddenLi.length) {
    const newLi = hiddenLi.clone().removeClass('d-none');
    hiddenLi.before(newLi);
}
                """
                            )
                            +"+"
                        }
                    }
                }
            }
        }
    }
}