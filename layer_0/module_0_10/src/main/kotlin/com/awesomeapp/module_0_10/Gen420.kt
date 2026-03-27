package com.awesomeapp.module_0_10

data class GenModel420(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService420 {
    fun process(model: GenModel420): GenModel420
    fun validate(model: GenModel420): Boolean
}

class GenServiceImpl420 : GenService420 {
    override fun process(model: GenModel420): GenModel420 = model.copy(active = true)
    override fun validate(model: GenModel420): Boolean = model.name.isNotEmpty()
}

sealed class GenResult420 {
    data class Success(val data: GenModel420) : GenResult420()
    data class Error(val message: String) : GenResult420()
    data object Loading : GenResult420()
}
