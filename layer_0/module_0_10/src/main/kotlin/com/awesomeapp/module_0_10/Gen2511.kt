package com.awesomeapp.module_0_10

data class GenModel2511(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2511 {
    fun process(model: GenModel2511): GenModel2511
    fun validate(model: GenModel2511): Boolean
}

class GenServiceImpl2511 : GenService2511 {
    override fun process(model: GenModel2511): GenModel2511 = model.copy(active = true)
    override fun validate(model: GenModel2511): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2511 {
    data class Success(val data: GenModel2511) : GenResult2511()
    data class Error(val message: String) : GenResult2511()
    data object Loading : GenResult2511()
}
