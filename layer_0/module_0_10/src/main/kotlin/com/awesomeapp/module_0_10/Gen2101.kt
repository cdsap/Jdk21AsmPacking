package com.awesomeapp.module_0_10

data class GenModel2101(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2101 {
    fun process(model: GenModel2101): GenModel2101
    fun validate(model: GenModel2101): Boolean
}

class GenServiceImpl2101 : GenService2101 {
    override fun process(model: GenModel2101): GenModel2101 = model.copy(active = true)
    override fun validate(model: GenModel2101): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2101 {
    data class Success(val data: GenModel2101) : GenResult2101()
    data class Error(val message: String) : GenResult2101()
    data object Loading : GenResult2101()
}
