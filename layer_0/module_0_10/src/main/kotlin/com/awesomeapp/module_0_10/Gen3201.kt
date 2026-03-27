package com.awesomeapp.module_0_10

data class GenModel3201(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3201 {
    fun process(model: GenModel3201): GenModel3201
    fun validate(model: GenModel3201): Boolean
}

class GenServiceImpl3201 : GenService3201 {
    override fun process(model: GenModel3201): GenModel3201 = model.copy(active = true)
    override fun validate(model: GenModel3201): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3201 {
    data class Success(val data: GenModel3201) : GenResult3201()
    data class Error(val message: String) : GenResult3201()
    data object Loading : GenResult3201()
}
