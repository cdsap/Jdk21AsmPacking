package com.awesomeapp.module_0_10

data class GenModel1270(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1270 {
    fun process(model: GenModel1270): GenModel1270
    fun validate(model: GenModel1270): Boolean
}

class GenServiceImpl1270 : GenService1270 {
    override fun process(model: GenModel1270): GenModel1270 = model.copy(active = true)
    override fun validate(model: GenModel1270): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1270 {
    data class Success(val data: GenModel1270) : GenResult1270()
    data class Error(val message: String) : GenResult1270()
    data object Loading : GenResult1270()
}
