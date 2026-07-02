package kcms.ui

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kcms.common.nullIfBlank
import kcms.common.nullIfNaN
import kcms.ui.cms.i18n.KcmsInternationalization
import kiss.gossr.spring.GetRoute
import kiss.gossr.spring.GossSpringRenderer
import kiss.gossr.spring.GossrSpringView
import kiss.gossr.spring.RoutesHelper
import org.springframework.core.convert.converter.Converter
import org.springframework.core.io.ClassPathResource
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.Cookie

@Suppress("FunctionNaming")
open class KcmsGossRenderer : GossSpringRenderer() {
    val i18n get() = KcmsInternationalization.instance

    override fun csrf(): Pair<String, String>? = (request()?.getAttribute("_csrf") as? CsrfToken)?.let {
        it.parameterName to it.token
    }

    fun Array<Cookie>.getValue(name: String): String? = this.firstOrNull {
        it.name.equals(name, ignoreCase = true)
    }?.value

    fun getResourceUrlWithVersion(path: String): String = resourceVersions.computeIfAbsent(path) {
        val resource = ClassPathResource("static$path")
        if(!isDevMode && resource.exists()) {
            val ver = resource.inputStream.use { DigestUtils.md5DigestAsHex(it) }
            "$path?v=$ver"
        } else {
            path
        }
    }

    override fun typeMoney(name: String?, value: Number?, required: Boolean) {
        val value = (value as? BigDecimal)
            ?: (value as? BigInteger)?.toBigDecimal()
            ?: value?.toDouble()?.nullIfNaN()
        type("text")
        pattern("^\\s*[0-9]*(\\.[0-9][0-9])?\\s*$")
        name(name)
        required(required)
        value(value?.let { "%.02f".format(it).replace(".00", "") })
        onBlur("this.value = this.value.trim()==='' ? '' : (this.value.trim()*1.0).toFixed(2)")
        onPaste("const i = this; setTimeout(function() { i.value = i.value.trim()==='' ? '' : (i.value.trim()*1.0).toFixed(2); }, 20)")
    }

    fun TH(title: String) = EL("TH") { +title }

    fun alt(alt: String) = attr("alt", alt)

    inline fun IMG(body: () -> Unit = {}) = EL("IMG", noBody = true, body = body)

    fun openModalOnClick(route: GetRoute) {
        onClick("return openCommonModal(event, ${toJson(RoutesHelper.buildRouteUri(route))})")
    }

    fun showErrorMessage(errorMessage: String?) {
        errorMessage.nullIfBlank()?.let {
            DIV("alert alert-danger") {
                +it
            }
        }
    }

    protected fun dropDownMenu(dropClass: String = "dropleft", body: () -> Unit) {
        DIV("btn-group $dropClass") {
            BUTTON {
                classes("btn btn-light btn-sm")
                style("border-radius: 50px;padding: 4px;")
                data("toggle", "dropdown")
                +"•••"
            }
            DIV("dropdown-menu", body)
        }
    }

    fun ajaxForm(condition: String = "true") {
        onSubmit("return ($condition) && ajaxFormSubmit(this, event)")
    }

    fun toJson(a: Any?): String = objectMapper.writeValueAsString(a)

    companion object {
        val isDevMode = System.getProperty("dev-mode") == "true"

        val objectMapper = createObjectMapper()

        val resourceVersions = ConcurrentHashMap<String, String>()

        private fun createObjectMapper(): ObjectMapper {
            val converter = ObjectMapper()
            converter.registerModule(JavaTimeModule())
            converter.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            converter.registerModule(KotlinModule.Builder().build())
            val hibernate5Module = Hibernate5Module()
            hibernate5Module.enable(Hibernate5Module.Feature.FORCE_LAZY_LOADING)
            converter.registerModule(hibernate5Module)
            converter.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            converter.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            converter.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            converter.registerModule(MultipartFileModule())

            return converter
        }
    }
}

@Suppress("UnnecessaryAbstractClass")
abstract class KcmsGossRendererView : KcmsGossRenderer(), GossrSpringView

@Component
class MultipartFileModule : com.fasterxml.jackson.databind.module.SimpleModule() {
    init {
        this.addSerializer(MultipartFileSerializer())
    }
}

class MultipartFileSerializer : StdSerializer<MultipartFile>(MultipartFile::class.java) {
    override fun serialize(value: MultipartFile?, gen: JsonGenerator?, provider: SerializerProvider?) {
        if(value == null || value.isEmpty) {
            gen?.writeNull()
        } else {
            gen?.writeObject(mapOf(
                "contentType" to value.contentType,
                "originalFilename" to value.originalFilename,
                "size" to value.size,
            ))
        }
    }
}

@Component
class LocalDateRequestParamConverter : Converter<String, LocalDate> {
    val iso: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun convert(source: String): LocalDate? {
        return source.nullIfBlank()?.let {
            LocalDate.parse(it, iso)
        }
    }
}
