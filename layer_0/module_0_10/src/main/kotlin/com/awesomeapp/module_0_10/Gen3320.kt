package com.awesomeapp.module_0_10

data class GenModel3320(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3320 {
    fun process(model: GenModel3320): GenModel3320
    fun validate(model: GenModel3320): Boolean
}

class GenServiceImpl3320 : GenService3320 {
    override fun process(model: GenModel3320): GenModel3320 = model.copy(active = true)
    override fun validate(model: GenModel3320): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3320 {
    data class Success(val data: GenModel3320) : GenResult3320()
    data class Error(val message: String) : GenResult3320()
    data object Loading : GenResult3320()
}
