package kcms.widgets

import kcms.ui.KCMSGossRenderer
import kcms.pages.PageProperty

enum class WidgetPropertyType {
    HTML, TEXT, DATE, NUMBER, ENUM
}

data class WidgetPropertyDescriptor(
    val key: String,
    val title: String,
    val type: WidgetPropertyType,
    val shared: Boolean,
    val enumCategory: String? = null,
)

interface WidgetRenderContext {
    fun getProperty(widgetId: String, propertyKey: String): PageProperty?
}

interface Widget {
    val id: String
    val title: String
    val properties: List<WidgetPropertyDescriptor>
}

interface ValueWidget<T> : Widget {
    fun getValue(context: WidgetRenderContext): T?
}

class NumberValueWidget(
    override val id: String,
    override val title: String,
    common: Boolean,
) : ValueWidget<Long> {
    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "value",
        title = "Number",
        type = WidgetPropertyType.HTML,
        shared = common
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    override fun getValue(context: WidgetRenderContext): Long? =
        context.getProperty(id, propertyDescriptor.key)?.number
}

class StringValueWidget(
    override val id: String,
    override val title: String,
    common: Boolean,
) : ValueWidget<String> {
    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "value",
        title = "Text",
        type = WidgetPropertyType.TEXT,
        shared = common
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    override fun getValue(context: WidgetRenderContext): String? =
        context.getProperty(id, propertyDescriptor.key)?.text
}

interface WidgetContainer : Widget {
    val children: List<Widget>?
}

open class HtmlContentWidget(
    override val id: String,
    override val title: String,
    common: Boolean,
) : WidgetContainer {
    override val children: List<Widget>? get() = null

    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "content",
        title = "HTML",
        type = WidgetPropertyType.HTML,
        shared = common
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    fun render(context: WidgetRenderContext, renderer: KCMSGossRenderer) = renderer.apply {
        noEscape(context.getProperty(id, propertyDescriptor.key)?.text)
    }
}

open class TextContentWidget(
    override val id: String,
    override val title: String,
    common: Boolean,
) : WidgetContainer  {
    override val children: List<Widget>? get() = null

    val propertyDescriptor = WidgetPropertyDescriptor(
        key = "content",
        title = "Text",
        type = WidgetPropertyType.TEXT,
        shared = common
    )

    override val properties: List<WidgetPropertyDescriptor> = listOf(propertyDescriptor)

    fun render(context: WidgetRenderContext, renderer: KCMSGossRenderer) = renderer.apply {
        +context.getProperty(id, propertyDescriptor.key)?.text
    }
}