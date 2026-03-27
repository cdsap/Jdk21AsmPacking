package com.awesomeapp.module_0_10

data class GenModel7(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService7 {
    fun process(model: GenModel7): GenModel7
    fun validate(model: GenModel7): Boolean
}

class GenServiceImpl7 : GenService7 {
    override fun process(model: GenModel7): GenModel7 = model.copy(active = true)
    override fun validate(model: GenModel7): Boolean = model.name.isNotEmpty()
}

sealed class GenResult7 {
    data class Success(val data: GenModel7) : GenResult7()
    data class Error(val message: String) : GenResult7()
    data object Loading : GenResult7()
}
