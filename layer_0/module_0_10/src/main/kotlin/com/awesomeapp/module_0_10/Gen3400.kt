package com.awesomeapp.module_0_10

data class GenModel3400(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3400 {
    fun process(model: GenModel3400): GenModel3400
    fun validate(model: GenModel3400): Boolean
}

class GenServiceImpl3400 : GenService3400 {
    override fun process(model: GenModel3400): GenModel3400 = model.copy(active = true)
    override fun validate(model: GenModel3400): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3400 {
    data class Success(val data: GenModel3400) : GenResult3400()
    data class Error(val message: String) : GenResult3400()
    data object Loading : GenResult3400()
}
