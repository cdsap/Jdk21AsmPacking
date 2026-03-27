package com.awesomeapp.module_0_10

data class GenModel2122(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2122 {
    fun process(model: GenModel2122): GenModel2122
    fun validate(model: GenModel2122): Boolean
}

class GenServiceImpl2122 : GenService2122 {
    override fun process(model: GenModel2122): GenModel2122 = model.copy(active = true)
    override fun validate(model: GenModel2122): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2122 {
    data class Success(val data: GenModel2122) : GenResult2122()
    data class Error(val message: String) : GenResult2122()
    data object Loading : GenResult2122()
}
