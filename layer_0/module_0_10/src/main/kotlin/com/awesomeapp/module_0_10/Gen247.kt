package com.awesomeapp.module_0_10

data class GenModel247(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService247 {
    fun process(model: GenModel247): GenModel247
    fun validate(model: GenModel247): Boolean
}

class GenServiceImpl247 : GenService247 {
    override fun process(model: GenModel247): GenModel247 = model.copy(active = true)
    override fun validate(model: GenModel247): Boolean = model.name.isNotEmpty()
}

sealed class GenResult247 {
    data class Success(val data: GenModel247) : GenResult247()
    data class Error(val message: String) : GenResult247()
    data object Loading : GenResult247()
}
