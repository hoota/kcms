package kcms.pages

import kcms.common.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "kcms_site_property")
data class SiteProperty(
    @Id
    override val key: String,
    override var text: String? = null,
    override var date: LocalDate? = null,
    override var number: BigDecimal? = null,
) : KcmsProperty

@Repository
interface SitePropertyRepository : CrudRepository<SiteProperty, String>