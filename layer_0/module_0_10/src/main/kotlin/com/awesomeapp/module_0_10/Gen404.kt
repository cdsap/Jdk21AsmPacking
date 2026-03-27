package com.awesomeapp.module_0_10

data class GenModel404(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService404 {
    fun process(model: GenModel404): GenModel404
    fun validate(model: GenModel404): Boolean
}

class GenServiceImpl404 : GenService404 {
    override fun process(model: GenModel404): GenModel404 = model.copy(active = true)
    override fun validate(model: GenModel404): Boolean = model.name.isNotEmpty()
}

sealed class GenResult404 {
    data class Success(val data: GenModel404) : GenResult404()
    data class Error(val message: String) : GenResult404()
    data object Loading : GenResult404()
}
