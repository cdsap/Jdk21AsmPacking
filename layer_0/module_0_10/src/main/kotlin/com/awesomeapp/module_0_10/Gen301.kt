package com.awesomeapp.module_0_10

data class GenModel301(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService301 {
    fun process(model: GenModel301): GenModel301
    fun validate(model: GenModel301): Boolean
}

class GenServiceImpl301 : GenService301 {
    override fun process(model: GenModel301): GenModel301 = model.copy(active = true)
    override fun validate(model: GenModel301): Boolean = model.name.isNotEmpty()
}

sealed class GenResult301 {
    data class Success(val data: GenModel301) : GenResult301()
    data class Error(val message: String) : GenResult301()
    data object Loading : GenResult301()
}
