package com.awesomeapp.module_0_10

data class GenModel2113(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2113 {
    fun process(model: GenModel2113): GenModel2113
    fun validate(model: GenModel2113): Boolean
}

class GenServiceImpl2113 : GenService2113 {
    override fun process(model: GenModel2113): GenModel2113 = model.copy(active = true)
    override fun validate(model: GenModel2113): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2113 {
    data class Success(val data: GenModel2113) : GenResult2113()
    data class Error(val message: String) : GenResult2113()
    data object Loading : GenResult2113()
}
