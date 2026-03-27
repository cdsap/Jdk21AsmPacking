package com.awesomeapp.module_0_10

data class GenModel3166(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3166 {
    fun process(model: GenModel3166): GenModel3166
    fun validate(model: GenModel3166): Boolean
}

class GenServiceImpl3166 : GenService3166 {
    override fun process(model: GenModel3166): GenModel3166 = model.copy(active = true)
    override fun validate(model: GenModel3166): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3166 {
    data class Success(val data: GenModel3166) : GenResult3166()
    data class Error(val message: String) : GenResult3166()
    data object Loading : GenResult3166()
}
