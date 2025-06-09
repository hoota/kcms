package kcms.widgets

import kcms.enums.KcmsEnumCategory
import kcms.pages.PageProperty
import kcms.pages.PageTemplateRenderContext
import kcms.ui.KcmsGossRenderer
import java.math.BigDecimal

enum class WidgetPropertyType {
    STRING, TEXT, DATE, NUMBER, ENUM, ENUMS_SET, WIDGET_COMPONENT, LIST, MAP
}

open class WidgetPropertyDescriptor(
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
    fun getProperty(widgetId: String, propertyKey: String): PageProperty?
    fun getProperty(widget: Widget, property: WidgetPropertyDescriptor): PageProperty?
}

interface Widget {
    val id: String
    val title: String
    val properties: List<WidgetPropertyDescriptor> get() = propertiesRows?.flatten() ?: emptyList()
    val propertiesRows: List<List<WidgetPropertyDescriptor>>? get() = null
}

interface ValueWidget<T> : Widget {
    fun getValue(context: WidgetRenderContext): T?
}

class NumberValueWidget(
    override val id: String,
    override val title: String,
    globalScope: Boolean,
) : ValueWidget<BigDecimal> {
    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "value",
        title = "Number",
        type = WidgetPropertyType.NUMBER,
        globalScope = globalScope
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    override fun getValue(context: WidgetRenderContext): BigDecimal? =
        context.getProperty(id, propertyDescriptor.key)?.number
}

interface WidgetContainer : Widget {
    val children: List<Widget>?
}

open class HtmlContentWidget(
    override val id: String,
    override val title: String,
    common: Boolean = false,
) : Widget {

    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "content",
        title = "HTML",
        type = WidgetPropertyType.TEXT,
        globalScope = common
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    fun render(context: WidgetRenderContext, renderer: KcmsGossRenderer) = renderer.apply {
        noEscape(context.getProperty(id, propertyDescriptor.key)?.text)
    }
}

open class TextContentWidget(
    override val id: String,
    override val title: String,
    type: WidgetPropertyType = WidgetPropertyType.TEXT,
    common: Boolean = false,
) : Widget  {

    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "content",
        title = "Text",
        type = type,
        globalScope = common
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    fun getValue(p: PageTemplateRenderContext): String? = p.getProperty(id, propertyDescriptor.key)?.text
}