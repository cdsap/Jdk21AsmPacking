package com.awesomeapp.module_0_10

data class GenModel3105(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3105 {
    fun process(model: GenModel3105): GenModel3105
    fun validate(model: GenModel3105): Boolean
}

class GenServiceImpl3105 : GenService3105 {
    override fun process(model: GenModel3105): GenModel3105 = model.copy(active = true)
    override fun validate(model: GenModel3105): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3105 {
    data class Success(val data: GenModel3105) : GenResult3105()
    data class Error(val message: String) : GenResult3105()
    data object Loading : GenResult3105()
}
