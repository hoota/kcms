package kcms.enums

import com.google.common.cache.CacheBuilder
import kcms.common.Caching
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class EnumValueService(
    val categories: List<CmsEnumCategory>,
    val enumValueRepository: EnumValueRepository,
) : Caching {

    private val enumValuesCacheByCategory = CacheBuilder.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .softValues()
        .build<String, List<EnumValue>>()

    fun getEnumValues(category: String): List<EnumValue> = enumValuesCacheByCategory.get(category) {
        enumValueRepository.findByCategory(category).map { it.copy() }
    }

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

interface CmsEnumCategory {
    val id: String
    val title: String
}