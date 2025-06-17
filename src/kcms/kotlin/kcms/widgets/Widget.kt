package kcms.widgets

import kcms.enums.EnumValue
import kcms.enums.EnumValueService
import kcms.enums.KcmsEnumCategory
import kcms.pages.PageProperty
import kcms.pages.PageTemplateRenderContext
import kcms.pages.asList
import kcms.pages.asMap
import kcms.ui.KcmsGossRenderer
import java.math.BigDecimal

enum class WidgetPropertyType {
    STRING, TEXT, DATE, NUMBER, ENUM, ENUMS_SET, WIDGET_COMPONENT, LIST, MAP
}

class WidgetPropertyDescriptor(
    val key: String,
    val title: String,
    val type: WidgetPropertyType,
    val globalScope: Boolean = false,
    val required: Boolean = false,
    val enumCategory: KcmsEnumCategory? = null,
    val numberMin: BigDecimal? = null,
    val numberStep: BigDecimal? = null,
    val widgetComponentClass: Class<out WidgetComponent>? = null,
    val columns: Int = 1,
)

interface WidgetRenderContext {
    fun getProperty(propertyKey: String): PageProperty?
    fun getProperty(property: WidgetPropertyDescriptor): PageProperty? = getProperty(property.key)
}

inline fun <T : Widget> WidgetRenderContext.asText(widget: T, propertyGetter: T.() -> WidgetPropertyDescriptor): String? {
    return getProperty(propertyGetter.invoke(widget))?.text
}

inline fun <T : Widget> WidgetRenderContext.asNumber(widget: T, propertyGetter: T.() -> WidgetPropertyDescriptor): BigDecimal? {
    return getProperty(propertyGetter.invoke(widget))?.number
}

inline fun <T : Widget> WidgetRenderContext.asLong(widget: T, propertyGetter: T.() -> WidgetPropertyDescriptor): Long? {
    return getProperty(propertyGetter.invoke(widget))?.number?.toLong()
}

inline fun <T : Widget> WidgetRenderContext.asInt(widget: T, propertyGetter: T.() -> WidgetPropertyDescriptor): Int? {
    return getProperty(propertyGetter.invoke(widget))?.number?.toInt()
}

inline fun <T : Widget> WidgetRenderContext.asList(widget: T, propertyGetter: T.() -> WidgetPropertyDescriptor): List<String>? {
    return getProperty(propertyGetter.invoke(widget))?.asList
}

inline fun <T : Widget> WidgetRenderContext.asMap(widget: T, propertyGetter: T.() -> WidgetPropertyDescriptor): Map<Long, String>? {
    return getProperty(propertyGetter.invoke(widget))?.asMap
}

inline fun <T : Widget> WidgetRenderContext.asEnum(enumCategory: KcmsEnumCategory, widget: T, propertyGetter: T.() -> WidgetPropertyDescriptor): EnumValue? {
    return getProperty(propertyGetter.invoke(widget))?.number?.toLong()?.let { id ->
        EnumValueService.instance.getEnumValues(enumCategory).firstOrNull { it.id == id }
    }
}


interface Widget {
    val title: String
    val properties: List<WidgetPropertyDescriptor> get() = propertiesRows?.flatten() ?: emptyList()
    val propertiesRows: List<List<WidgetPropertyDescriptor>>? get() = null
}

interface ValueWidget<T> : Widget {
    fun getValue(context: WidgetRenderContext): T?
}

class NumberValueWidget(
    val widgetIt: String,
    override val title: String,
    globalScope: Boolean,
) : ValueWidget<BigDecimal> {
    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "$widgetIt.value",
        title = "Number",
        type = WidgetPropertyType.NUMBER,
        globalScope = globalScope
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    override fun getValue(context: WidgetRenderContext): BigDecimal? =
        context.getProperty(propertyDescriptor.key)?.number
}

interface WidgetContainer : Widget {
    val children: List<Widget>?
}

open class HtmlContentWidget(
    val widgetId: String,
    override val title: String,
    common: Boolean = false,
) : Widget {

    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "$widgetId.content",
        title = "HTML",
        type = WidgetPropertyType.TEXT,
        globalScope = common
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    fun render(context: WidgetRenderContext, renderer: KcmsGossRenderer) = renderer.apply {
        noEscape(context.getProperty(propertyDescriptor.key)?.text)
    }
}

open class TextContentWidget(
    val widgetId: String,
    override val title: String,
    type: WidgetPropertyType = WidgetPropertyType.TEXT,
    common: Boolean = false,
) : Widget  {

    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "$widgetId.content",
        title = "Text",
        type = type,
        globalScope = common
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    fun getValue(p: PageTemplateRenderContext): String? = p.getProperty(propertyDescriptor.key)?.text
}