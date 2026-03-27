package com.awesomeapp.module_0_10

data class GenModel3404(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3404 {
    fun process(model: GenModel3404): GenModel3404
    fun validate(model: GenModel3404): Boolean
}

class GenServiceImpl3404 : GenService3404 {
    override fun process(model: GenModel3404): GenModel3404 = model.copy(active = true)
    override fun validate(model: GenModel3404): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3404 {
    data class Success(val data: GenModel3404) : GenResult3404()
    data class Error(val message: String) : GenResult3404()
    data object Loading : GenResult3404()
}
