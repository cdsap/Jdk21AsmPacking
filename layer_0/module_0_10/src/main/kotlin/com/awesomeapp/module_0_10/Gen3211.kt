package com.awesomeapp.module_0_10

data class GenModel3211(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3211 {
    fun process(model: GenModel3211): GenModel3211
    fun validate(model: GenModel3211): Boolean
}

class GenServiceImpl3211 : GenService3211 {
    override fun process(model: GenModel3211): GenModel3211 = model.copy(active = true)
    override fun validate(model: GenModel3211): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3211 {
    data class Success(val data: GenModel3211) : GenResult3211()
    data class Error(val message: String) : GenResult3211()
    data object Loading : GenResult3211()
}
