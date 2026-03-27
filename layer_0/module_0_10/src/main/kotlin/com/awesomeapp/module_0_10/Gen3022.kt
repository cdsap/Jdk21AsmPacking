package com.awesomeapp.module_0_10

data class GenModel3022(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3022 {
    fun process(model: GenModel3022): GenModel3022
    fun validate(model: GenModel3022): Boolean
}

class GenServiceImpl3022 : GenService3022 {
    override fun process(model: GenModel3022): GenModel3022 = model.copy(active = true)
    override fun validate(model: GenModel3022): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3022 {
    data class Success(val data: GenModel3022) : GenResult3022()
    data class Error(val message: String) : GenResult3022()
    data object Loading : GenResult3022()
}
