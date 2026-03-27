package com.awesomeapp.module_0_10

data class GenModel310(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService310 {
    fun process(model: GenModel310): GenModel310
    fun validate(model: GenModel310): Boolean
}

class GenServiceImpl310 : GenService310 {
    override fun process(model: GenModel310): GenModel310 = model.copy(active = true)
    override fun validate(model: GenModel310): Boolean = model.name.isNotEmpty()
}

sealed class GenResult310 {
    data class Success(val data: GenModel310) : GenResult310()
    data class Error(val message: String) : GenResult310()
    data object Loading : GenResult310()
}
