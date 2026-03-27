package com.awesomeapp.module_0_10

data class GenModel1602(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1602 {
    fun process(model: GenModel1602): GenModel1602
    fun validate(model: GenModel1602): Boolean
}

class GenServiceImpl1602 : GenService1602 {
    override fun process(model: GenModel1602): GenModel1602 = model.copy(active = true)
    override fun validate(model: GenModel1602): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1602 {
    data class Success(val data: GenModel1602) : GenResult1602()
    data class Error(val message: String) : GenResult1602()
    data object Loading : GenResult1602()
}
