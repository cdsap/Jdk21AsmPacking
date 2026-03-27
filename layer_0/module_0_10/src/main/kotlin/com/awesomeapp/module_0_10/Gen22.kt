package com.awesomeapp.module_0_10

data class GenModel22(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService22 {
    fun process(model: GenModel22): GenModel22
    fun validate(model: GenModel22): Boolean
}

class GenServiceImpl22 : GenService22 {
    override fun process(model: GenModel22): GenModel22 = model.copy(active = true)
    override fun validate(model: GenModel22): Boolean = model.name.isNotEmpty()
}

sealed class GenResult22 {
    data class Success(val data: GenModel22) : GenResult22()
    data class Error(val message: String) : GenResult22()
    data object Loading : GenResult22()
}
