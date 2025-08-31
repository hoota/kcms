package kcms.pages

import kcms.common.nullIfEmpty
import kcms.enums.EnumValueService
import kcms.ui.KcmsGossRenderer
import kcms.ui.cms.i18n.KcmsInternationalization
import kcms.widgets.PagePropertyDescriptor
import kcms.widgets.Widget
import kcms.widgets.WidgetComponentService
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class KcmsPropertiesEditBlock(
    val route: WidgetPropertiesSaveRoute,
    val values: Map<String, KcmsProperty>
) : KcmsGossRenderer() {

    fun drawWidgets(widgets: List<Widget>?) {
        widgets?.forEach { w ->
            H5 { +w.title }
            w.forEach { r ->
                DIV("row ml-2") {
                    r.forEach { c ->
                        DIV(if(c.width == null) "col-12 col-md" else "col-12 col-md-${c.width}") {
                            draw(c.pds)
                        }
                    }
                }
            }
        }
    }

    fun draw(
        properties: Array<out PagePropertyDescriptor>
    ) {
        properties.forEach { p ->
            val v = values.get(p.key)
            DIV("form-group") {
                LABEL("font-weight-bold") {
                    +p.title
                }
                when(p) {
                    is PagePropertyDescriptor.AsText -> if(p.lines <= 1) INPUT("form-control") {
                        namePrefix(route::properties) { name(p.key) }
                        type("text")
                        required(p.required)
                        value(v?.text)
                    } else {
                        val id = UUID.randomUUID().toString().replace("-", "")
                        TEXTAREA("form-control") {
                            id(id)
                            style("width: 100%")
                            attr("rows", p.lines)
                            namePrefix(route::properties) { name(p.key) }
                            required(p.required)
                            +v?.text
                        }
                        if(p.htmlEditor) SCRIPT(code = """$('#$id').trumbowyg({lang:'${KcmsInternationalization.language}'});""")
                    }

                    is PagePropertyDescriptor.AsBool -> SELECT("form-control") {
                        namePrefix(route::properties) { name(p.key) }
                        OPTION("true", p.trueLabel ?: i18n.yes, selected = v?.asBool == true)
                        OPTION("false", p.trueLabel ?: i18n.no, selected = v?.asBool != true)
                    }

                    is PagePropertyDescriptor.AsDate -> INPUT("form-control") {
                        namePrefix(route::properties) { name(p.key) }
                        type("date")
                        required(p.required)
                        value(v?.date)
                    }

                    is PagePropertyDescriptor.AsInt -> INPUT("form-control") {
                        namePrefix(route::properties) { name(p.key) }
                        type("number")
                        required(p.required)
                        value(v?.number)
                        min(p.min.toString())
                        max(p.max.toString())
                    }

                    is PagePropertyDescriptor.AsLong -> INPUT("form-control") {
                        namePrefix(route::properties) { name(p.key) }
                        type("number")
                        required(p.required)
                        value(v?.number)
                        min(p.min.toString())
                        max(p.max.toString())
                    }

                    is PagePropertyDescriptor.AsNumber -> INPUT("form-control") {
                        namePrefix(route::properties) { name(p.key) }
                        type("number")
                        required(p.required)
                        value(v?.number)
                        min(p.min?.toString())
                        max(p.max?.toString())
                        step(p.step?.toString())
                    }

                    is PagePropertyDescriptor.AsEnum -> SELECT("form-control") {
                        namePrefix(route::properties) { name(p.key) }
                        if(!p.required) OPTION("--")
                        EnumValueService.instance.getEnumValues(p.category).forEach { e ->
                            OPTION(e.id, e.value, selected = v?.number?.toLong() == e.id)
                        }
                    }

                    is PagePropertyDescriptor.AsEnumSet -> namePrefix(route::listProperties) {
                        drawEnumsSetInput(p, v?.asList?.mapNotNull { it.toLongOrNull() } ?: emptyList())
                    }

                    is PagePropertyDescriptor.AsComponent -> SELECT("form-control") {
                        namePrefix(route::properties) { name(p.key) }
                        if(!p.required) OPTION("--")
                        p.componentClass?.let { clazz ->
                            WidgetComponentService.instance.getWidgetComponents(clazz).forEach { wc ->
                                OPTION(wc.key, wc.value.title, selected = v?.text == wc.key)
                            }
                        }
                    }

                    is PagePropertyDescriptor.AsList -> namePrefix(route::listProperties) {
                        drawListInput(p, v?.asList.nullIfEmpty() ?: listOf(""))
                    }

                    is PagePropertyDescriptor.AsMap -> namePrefix(route::enumMapProperties) {
                        drawEnumMapInput(p, v?.asMap)
                    }
                }
            }
        }
    }

    private fun drawEnumsSetInput(
        p: PagePropertyDescriptor.AsEnumSet,
        values: List<Long>
    ) = DIV("row") {
        val enumValues = EnumValueService.instance.getEnumValues(p.category)
        (0 until p.columns).forEach { c ->
            DIV("col-12 col-md") {
                HIDDEN(p.key, "")
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
        p: PagePropertyDescriptor.AsMap,
        values: Map<Long, String>?,
    ) = DIV("row") {
        val enumValues = EnumValueService.instance.getEnumValues(p.category)
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
        p: PagePropertyDescriptor.AsList,
        values: List<String>,
    ) {
        val values = values.chunked(ceil(values.size.toDouble() / p.columns).roundToInt())

        DIV("row") {
            values.forEach { strings ->
                UL("col-12 col-md ul ml-3") {
                    strings.forEach { v ->
                        LI("mb-1") {
                            INPUT("form-control") {
                                name(p.key)
                                type("text")
                                value(v)
                            }
                        }
                    }
                }
            }
        }

        DIV("row") {
            values.forEachIndexed { index, _ ->
                UL("col-12 col-md ul ml-3") {
                    if(index == 0) {
                        LI("d-none mb-1") {
                            INPUT("form-control") {
                                name(p.key)
                                type("text")
                            }
                        }
                        LI {
                            SPAN("btn btn-sm btn-outline-primary") {
                                onClick("""
const ul = $(this).closest('.ul');
const hiddenLi = ul.find('li.d-none').first();
if (hiddenLi.length) {
    const newLi = hiddenLi.clone().removeClass('d-none');
    hiddenLi.before(newLi);
}""")
                                +"+"
                            }
                        }
                    }
                }
            }
        }
    }
}