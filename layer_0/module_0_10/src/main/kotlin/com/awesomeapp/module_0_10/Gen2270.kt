package com.awesomeapp.module_0_10

data class GenModel2270(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2270 {
    fun process(model: GenModel2270): GenModel2270
    fun validate(model: GenModel2270): Boolean
}

class GenServiceImpl2270 : GenService2270 {
    override fun process(model: GenModel2270): GenModel2270 = model.copy(active = true)
    override fun validate(model: GenModel2270): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2270 {
    data class Success(val data: GenModel2270) : GenResult2270()
    data class Error(val message: String) : GenResult2270()
    data object Loading : GenResult2270()
}
