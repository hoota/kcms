package kcms.enums

import kcms.common.Caching
import kcms.common.CrudRepository
import kcms.common.OneValueCache
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.io.Serializable
import java.util.concurrent.TimeUnit
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "kcms_enum_category")
data class EnumCategory(
    @Id
    override val id: String,
    override var title: String
) : KcmsEnumCategory, Serializable

@Repository
interface EnumCategoryRepository : CrudRepository<EnumCategory, String>

@Component
class DatabaseEnumCategoryProvider(
    private val enumCategoryRepository: EnumCategoryRepository,
) : KcmsEnumCategoryProvider, Caching {

    private val cache = OneValueCache(
        timeout = 24,
        timeunit = TimeUnit.HOURS
    ) {
        enumCategoryRepository.findAll().map { it.copy() }
    }

    override fun resetCaches() {
        cache.reset()
    }

    override fun getCategories(): List<KcmsEnumCategory> {
        return cache.value ?: emptyList()
    }
}