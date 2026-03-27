package com.awesomeapp.module_0_10

data class GenModel3160(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3160 {
    fun process(model: GenModel3160): GenModel3160
    fun validate(model: GenModel3160): Boolean
}

class GenServiceImpl3160 : GenService3160 {
    override fun process(model: GenModel3160): GenModel3160 = model.copy(active = true)
    override fun validate(model: GenModel3160): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3160 {
    data class Success(val data: GenModel3160) : GenResult3160()
    data class Error(val message: String) : GenResult3160()
    data object Loading : GenResult3160()
}
