package com.awesomeapp.module_0_10

data class GenModel3016(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3016 {
    fun process(model: GenModel3016): GenModel3016
    fun validate(model: GenModel3016): Boolean
}

class GenServiceImpl3016 : GenService3016 {
    override fun process(model: GenModel3016): GenModel3016 = model.copy(active = true)
    override fun validate(model: GenModel3016): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3016 {
    data class Success(val data: GenModel3016) : GenResult3016()
    data class Error(val message: String) : GenResult3016()
    data object Loading : GenResult3016()
}
