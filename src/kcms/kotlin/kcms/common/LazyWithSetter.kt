package kcms.common

import kotlin.reflect.KProperty

abstract class LazyWithSetter<T> {
    var inited: Boolean = false
    var value: T? = null

    operator fun setValue(a: Any, p: KProperty<*>, v: T?) = synchronized(this) {
        value = v
        onValueSet(v)
        inited = true
    }


    operator fun getValue(a: Any, p: KProperty<*>): T? = synchronized(this) {
        if(!inited) {
            value = initValue()
            inited = true
        }

        value
    }

    abstract fun initValue(): T?
    abstract fun onValueSet(v: T?)
}