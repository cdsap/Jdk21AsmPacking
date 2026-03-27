package com.awesomeapp.module_0_10

data class GenModel1984(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1984 {
    fun process(model: GenModel1984): GenModel1984
    fun validate(model: GenModel1984): Boolean
}

class GenServiceImpl1984 : GenService1984 {
    override fun process(model: GenModel1984): GenModel1984 = model.copy(active = true)
    override fun validate(model: GenModel1984): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1984 {
    data class Success(val data: GenModel1984) : GenResult1984()
    data class Error(val message: String) : GenResult1984()
    data object Loading : GenResult1984()
}
