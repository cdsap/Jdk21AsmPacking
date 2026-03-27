package com.awesomeapp.module_0_10

data class GenModel3180(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3180 {
    fun process(model: GenModel3180): GenModel3180
    fun validate(model: GenModel3180): Boolean
}

class GenServiceImpl3180 : GenService3180 {
    override fun process(model: GenModel3180): GenModel3180 = model.copy(active = true)
    override fun validate(model: GenModel3180): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3180 {
    data class Success(val data: GenModel3180) : GenResult3180()
    data class Error(val message: String) : GenResult3180()
    data object Loading : GenResult3180()
}
