package com.awesomeapp.module_0_10

data class GenModel3406(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3406 {
    fun process(model: GenModel3406): GenModel3406
    fun validate(model: GenModel3406): Boolean
}

class GenServiceImpl3406 : GenService3406 {
    override fun process(model: GenModel3406): GenModel3406 = model.copy(active = true)
    override fun validate(model: GenModel3406): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3406 {
    data class Success(val data: GenModel3406) : GenResult3406()
    data class Error(val message: String) : GenResult3406()
    data object Loading : GenResult3406()
}
