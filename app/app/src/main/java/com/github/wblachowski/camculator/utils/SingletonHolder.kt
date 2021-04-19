package com.github.wblachowski.camculator.utils

open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance() = instance!!

    fun init(arg: A) =
            instance ?: synchronized(this) {
                val i2 = instance
                if (i2 != null) {
                    i2
                } else {
                    val created = creator!!(arg)
                    instance = created
                    creator = null
                    created
                }
            }
}

open class ArglessSingletonHolder<out T : Any>(creator: () -> T) {
    private var creator: (() -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance() =
            instance ?: synchronized(this) {
                val i2 = instance
                if (i2 != null) {
                    i2
                } else {
                    val created = creator!!()
                    instance = created
                    creator = null
                    created
                }
            }
}