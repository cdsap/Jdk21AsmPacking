package com.awesomeapp.module_0_10

data class GenModel1(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1 {
    fun process(model: GenModel1): GenModel1
    fun validate(model: GenModel1): Boolean
}

class GenServiceImpl1 : GenService1 {
    override fun process(model: GenModel1): GenModel1 = model.copy(active = true)
    override fun validate(model: GenModel1): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1 {
    data class Success(val data: GenModel1) : GenResult1()
    data class Error(val message: String) : GenResult1()
    data object Loading : GenResult1()
}
