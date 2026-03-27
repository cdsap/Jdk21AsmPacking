package com.awesomeapp.module_0_10

data class GenModel3280(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3280 {
    fun process(model: GenModel3280): GenModel3280
    fun validate(model: GenModel3280): Boolean
}

class GenServiceImpl3280 : GenService3280 {
    override fun process(model: GenModel3280): GenModel3280 = model.copy(active = true)
    override fun validate(model: GenModel3280): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3280 {
    data class Success(val data: GenModel3280) : GenResult3280()
    data class Error(val message: String) : GenResult3280()
    data object Loading : GenResult3280()
}
