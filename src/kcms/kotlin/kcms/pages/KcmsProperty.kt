package kcms.pages

import kcms.ui.KcmsGossRenderer
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

interface KcmsProperty : Serializable {
    val key: String
    var text: String?
    var date: LocalDate?
    var number: BigDecimal?
}

var KcmsProperty.asList: List<String>?
    get() = try {
        KcmsGossRenderer.objectMapper.readValue(text, PagePropertyStringListValue::class.java)
    }catch(e: Exception) {
        null
    }
    set(v) {
        text = KcmsGossRenderer.objectMapper.writeValueAsString(v)
    }

var KcmsProperty.asMap: Map<Long, String>?
    get() = try {
        KcmsGossRenderer.objectMapper.readValue(text, PagePropertyMapValue::class.java)
    }catch(e: Exception) {
        null
    }
    set(v) {
        text = KcmsGossRenderer.objectMapper.writeValueAsString(v)
    }

var KcmsProperty.asBool: Boolean
    get() = text == "true"
    set(v) {
        text = v.toString()
    }

class PagePropertyStringListValue : ArrayList<String>()

class PagePropertyMapValue : LinkedHashMap<Long, String>()
