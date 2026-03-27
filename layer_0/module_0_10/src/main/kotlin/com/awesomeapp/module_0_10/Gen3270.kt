package com.awesomeapp.module_0_10

data class GenModel3270(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3270 {
    fun process(model: GenModel3270): GenModel3270
    fun validate(model: GenModel3270): Boolean
}

class GenServiceImpl3270 : GenService3270 {
    override fun process(model: GenModel3270): GenModel3270 = model.copy(active = true)
    override fun validate(model: GenModel3270): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3270 {
    data class Success(val data: GenModel3270) : GenResult3270()
    data class Error(val message: String) : GenResult3270()
    data object Loading : GenResult3270()
}
