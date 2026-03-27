package com.awesomeapp.module_0_10

data class GenModel3204(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3204 {
    fun process(model: GenModel3204): GenModel3204
    fun validate(model: GenModel3204): Boolean
}

class GenServiceImpl3204 : GenService3204 {
    override fun process(model: GenModel3204): GenModel3204 = model.copy(active = true)
    override fun validate(model: GenModel3204): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3204 {
    data class Success(val data: GenModel3204) : GenResult3204()
    data class Error(val message: String) : GenResult3204()
    data object Loading : GenResult3204()
}
