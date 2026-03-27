package com.awesomeapp.module_0_10

data class GenModel3012(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3012 {
    fun process(model: GenModel3012): GenModel3012
    fun validate(model: GenModel3012): Boolean
}

class GenServiceImpl3012 : GenService3012 {
    override fun process(model: GenModel3012): GenModel3012 = model.copy(active = true)
    override fun validate(model: GenModel3012): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3012 {
    data class Success(val data: GenModel3012) : GenResult3012()
    data class Error(val message: String) : GenResult3012()
    data object Loading : GenResult3012()
}
