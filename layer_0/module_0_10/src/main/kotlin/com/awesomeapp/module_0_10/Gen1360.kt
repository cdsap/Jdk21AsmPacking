package com.awesomeapp.module_0_10

data class GenModel1360(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1360 {
    fun process(model: GenModel1360): GenModel1360
    fun validate(model: GenModel1360): Boolean
}

class GenServiceImpl1360 : GenService1360 {
    override fun process(model: GenModel1360): GenModel1360 = model.copy(active = true)
    override fun validate(model: GenModel1360): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1360 {
    data class Success(val data: GenModel1360) : GenResult1360()
    data class Error(val message: String) : GenResult1360()
    data object Loading : GenResult1360()
}
