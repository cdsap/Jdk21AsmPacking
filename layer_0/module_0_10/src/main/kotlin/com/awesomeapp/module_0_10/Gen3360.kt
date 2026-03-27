package com.awesomeapp.module_0_10

data class GenModel3360(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3360 {
    fun process(model: GenModel3360): GenModel3360
    fun validate(model: GenModel3360): Boolean
}

class GenServiceImpl3360 : GenService3360 {
    override fun process(model: GenModel3360): GenModel3360 = model.copy(active = true)
    override fun validate(model: GenModel3360): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3360 {
    data class Success(val data: GenModel3360) : GenResult3360()
    data class Error(val message: String) : GenResult3360()
    data object Loading : GenResult3360()
}
