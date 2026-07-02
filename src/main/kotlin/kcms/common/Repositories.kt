package kcms.common

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import java.io.Serializable
import java.util.*

@NoRepositoryBean
interface ReadOnlyRepository<T, ID> : Repository<T, ID> {
    fun findById(id: ID): Optional<T>

    fun existsById(id: ID): Boolean

    fun findAll(): Iterable<T>

    fun count(): Long
}

@NoRepositoryBean
interface CruRepository<T, ID> : ReadOnlyRepository<T, ID> {
    fun <S : T> save(e: S): S
    fun <S : T> saveAll(e: Iterable<S>): Iterable<S>

    fun <S : T> saveAndFlush(entity: S): S
    fun <S : T> saveAllAndFlush(entities: Iterable<S>): Iterable<S>

}

@NoRepositoryBean
/** Type-safe alternative to original Spring CrudRepository */
interface CrudRepository<T, ID> : CruRepository<T, ID> {
    fun deleteById(id: ID)

    fun delete(entity: T)

    fun deleteAll(ids: Iterable<T>)

    fun deleteAll()
}

interface EntityWithLongId : Serializable {
    val id: Long
}

@NoRepositoryBean
interface LongIdCrudRepository<T : EntityWithLongId> : CrudRepository<T, Long> {
    fun findByIdIn(ids: Iterable<Long>): List<T>
    fun findByIdIsGreaterThanOrderById(fromId: Long, page: Pageable): List<T>
}

fun <T : EntityWithLongId> LongIdCrudRepository<T>.chunked(size: Int = 1000): Sequence<T> = sequence {
    var fromId = Long.MIN_VALUE
    val page = PageRequest.ofSize(size)

    while(true) {
        val list = findByIdIsGreaterThanOrderById(fromId, page)
        fromId = list.lastOrNull()?.id ?: break
        yieldAll(list)
    }
}

fun <T : EntityWithLongId> LongIdCrudRepository<T>.chunkedLists(size: Int = 1000): Sequence<List<T>> = sequence {
    var fromId = Long.MIN_VALUE
    val page = PageRequest.ofSize(size)

    while(true) {
        val list = findByIdIsGreaterThanOrderById(fromId, page)
        fromId = list.lastOrNull()?.id ?: break
        yield(list)
    }
}


interface EntityWithStringId : Serializable {
    val id: String
}

@NoRepositoryBean
interface StringIdCrudRepository<T : EntityWithStringId> : CrudRepository<T, String> {
    fun findByIdIn(ids: Iterable<String>): List<T>
    fun findByIdIsGreaterThanOrderById(fromId: String, page: Pageable): List<T>
}

fun <T : EntityWithStringId> StringIdCrudRepository<T>.chunked(size: Int = 1000): Sequence<T> = sequence {
    var fromId = ""
    val page = PageRequest.ofSize(size)

    while(true) {
        val list = findByIdIsGreaterThanOrderById(fromId, page)
        fromId = list.lastOrNull()?.id ?: break
        yieldAll(list)
    }
}
