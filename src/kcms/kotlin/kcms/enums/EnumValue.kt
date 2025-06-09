package kcms.enums

import kcms.common.EntityWithLongId
import kcms.common.LongIdCrudRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "enum")
data class EnumValue(
    @Id
    override val id: Long = 0,
    var category: String,
    var value: String,
    @Column(name = "ord")
    var order: Int = 0,
) : EntityWithLongId {
    companion object {
        const val GENERATOR_NAME = "enum_id_seq"
    }
}

@Repository
interface EnumValueRepository : LongIdCrudRepository<EnumValue> {
    fun findByCategory(category: String): List<EnumValue>

    @Query("""SELECT nextval('${EnumValue.GENERATOR_NAME}')""", nativeQuery = true)
    fun nextId(): Long
}