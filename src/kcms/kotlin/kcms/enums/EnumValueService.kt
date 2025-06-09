package kcms.enums

import com.google.common.cache.CacheBuilder
import kcms.common.Caching
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class EnumValueService(
    val categories: List<KcmsEnumCategory>,
    val enumValueRepository: EnumValueRepository,
) : Caching {

    private val enumValuesCacheByCategory = CacheBuilder.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .softValues()
        .build<String, List<EnumValue>>()

    fun getEnumValues(category: String): List<EnumValue> = enumValuesCacheByCategory.get(category) {
        enumValueRepository.findByCategory(category).sortedBy { it.order }.map { it.copy() }
    }

    fun getEnumValues(category: KcmsEnumCategory?): List<EnumValue> = category?.let {
        getEnumValues(it.id)
    } ?: emptyList()

    override fun resetCaches() {
        enumValuesCacheByCategory.invalidateAll()
    }

    @PostConstruct
    fun postConstruct() {
        instance = this
    }

    companion object {
        lateinit var instance: EnumValueService
    }
}

interface KcmsEnumCategory {
    val id: String
    val title: String
}