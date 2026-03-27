package com.awesomeapp.module_0_10

data class GenModel3013(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3013 {
    fun process(model: GenModel3013): GenModel3013
    fun validate(model: GenModel3013): Boolean
}

class GenServiceImpl3013 : GenService3013 {
    override fun process(model: GenModel3013): GenModel3013 = model.copy(active = true)
    override fun validate(model: GenModel3013): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3013 {
    data class Success(val data: GenModel3013) : GenResult3013()
    data class Error(val message: String) : GenResult3013()
    data object Loading : GenResult3013()
}
