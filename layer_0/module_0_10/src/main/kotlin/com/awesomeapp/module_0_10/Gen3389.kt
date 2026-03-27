package com.awesomeapp.module_0_10

data class GenModel3389(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3389 {
    fun process(model: GenModel3389): GenModel3389
    fun validate(model: GenModel3389): Boolean
}

class GenServiceImpl3389 : GenService3389 {
    override fun process(model: GenModel3389): GenModel3389 = model.copy(active = true)
    override fun validate(model: GenModel3389): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3389 {
    data class Success(val data: GenModel3389) : GenResult3389()
    data class Error(val message: String) : GenResult3389()
    data object Loading : GenResult3389()
}
