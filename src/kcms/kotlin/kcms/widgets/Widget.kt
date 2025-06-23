package kcms.widgets

import kcms.enums.EnumValue
import kcms.enums.EnumValueService
import kcms.enums.KcmsEnumCategory
import kcms.pages.PageProperty
import kcms.pages.asList
import kcms.pages.asMap
import kcms.ui.KcmsGossRenderer
import java.math.BigDecimal
import java.time.LocalDate

// enum class PagePropertyType {
//     STRING, TEXT, DATE, NUMBER, ENUM, ENUMS_SET, WIDGET_COMPONENT, LIST, MAP
// }

sealed interface PagePropertyDescriptor {
    val key: String
    val title: String

    data class AsText(
        override val key: String,
        override val title: String,
        val lines: Int = 1,
        val required: Boolean = false,
    ) : PagePropertyDescriptor

    data class AsDate(
        override val key: String,
        override val title: String,
        val required: Boolean = false,
    ) : PagePropertyDescriptor

    data class AsInt(
        override val key: String,
        override val title: String,
        val required: Boolean = false,
        val min: Int = Int.MIN_VALUE,
        val max: Int = Int.MAX_VALUE
    ) : PagePropertyDescriptor

    data class AsLong(
        override val key: String,
        override val title: String,
        val required: Boolean = false,
        val min: Long = Long.MIN_VALUE,
        val max: Long = Long.MAX_VALUE
    ) : PagePropertyDescriptor

    data class AsNumber(
        override val key: String,
        override val title: String,
        val required: Boolean = false,
        val min: BigDecimal? = null,
        val max: BigDecimal? = null,
        val step: BigDecimal? = null,
    ) : PagePropertyDescriptor

    data class AsEnum(
        override val key: String,
        override val title: String,
        val required: Boolean = false,
        val category: KcmsEnumCategory,
    ) : PagePropertyDescriptor

    data class AsEnumSet(
        override val key: String,
        override val title: String,
        val category: KcmsEnumCategory,
        val columns: Int = 1
    ) : PagePropertyDescriptor

    data class AsComponent(
        override val key: String,
        override val title: String,
        val required: Boolean = false,
        val componentClass: Class<out WidgetComponent>? = null,
    ) : PagePropertyDescriptor

    data class AsList(
        override val key: String,
        override val title: String,
        val columns: Int = 1
    ) : PagePropertyDescriptor

    data class AsMap(
        override val key: String,
        override val title: String,
        val category: KcmsEnumCategory,
        val columns: Int = 1
    ) : PagePropertyDescriptor

}

interface WidgetRenderContext {
    fun getProperty(propertyKey: String): PageProperty?
    fun getProperty(property: PagePropertyDescriptor): PageProperty? = getProperty(property.key)

    fun valueOf(p: PagePropertyDescriptor.AsText): String? {
        return getProperty(p)?.text
    }

    fun valueOf(p: PagePropertyDescriptor.AsDate): LocalDate? {
        return getProperty(p)?.date
    }

    fun valueOf(p: PagePropertyDescriptor.AsNumber): BigDecimal? {
        return getProperty(p)?.number
    }

    fun valueOf(p: PagePropertyDescriptor.AsLong): Long? {
        return getProperty(p)?.number?.toLong()
    }

    fun valueOf(p: PagePropertyDescriptor.AsInt): Int? {
        return getProperty(p)?.number?.toInt()
    }

    fun valueOf(p: PagePropertyDescriptor.AsList): List<String>? {
        return getProperty(p)?.asList
    }

    fun valueOf(p: PagePropertyDescriptor.AsMap): Map<Long, String>? {
        return getProperty(p)?.asMap
    }

    fun valueOf(p: PagePropertyDescriptor.AsEnum): EnumValue? {
        return getProperty(p)?.number?.toLong()?.let { id ->
            EnumValueService.instance.getEnumValues(p.category).firstOrNull { it.id == id }
        }
    }
}

open class Widget(
    val title: String,
    val body: Widget.() -> Unit
) : ArrayList<Widget.Row>() {

    init { body() }

    class Row : ArrayList<Column>() {
        fun col(width: Int, vararg pds: PagePropertyDescriptor) {
            add(Column(width, pds))
        }
        fun col(vararg pds: PagePropertyDescriptor) {
            add(Column(width = null, pds))
        }
    }
    class Column(
        val width: Int?,
        val pds: Array<out PagePropertyDescriptor>
    )

    fun row(body: Widget.Row.() -> Unit) {
        add(Row().also(body))
    }

    fun row(vararg pds: PagePropertyDescriptor) {
        add(Row().also {
            it.addAll(pds.map { Column(null, arrayOf(it)) })
        })
    }
}

open class HtmlContentWidget(
    widgetId: String,
    title: String,
    private val propertyDescriptor: PagePropertyDescriptor.AsText = PagePropertyDescriptor.AsText(
        key = "$widgetId.content",
        title = "HTML",
        lines = 10
    )
) : Widget(title, {
    row(propertyDescriptor)
}) {

    fun render(context: WidgetRenderContext, renderer: KcmsGossRenderer) = renderer.apply {
        noEscape(context.getProperty(propertyDescriptor.key)?.text)
    }
}
