package com.awesomeapp.module_0_10

data class GenModel2017(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2017 {
    fun process(model: GenModel2017): GenModel2017
    fun validate(model: GenModel2017): Boolean
}

class GenServiceImpl2017 : GenService2017 {
    override fun process(model: GenModel2017): GenModel2017 = model.copy(active = true)
    override fun validate(model: GenModel2017): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2017 {
    data class Success(val data: GenModel2017) : GenResult2017()
    data class Error(val message: String) : GenResult2017()
    data object Loading : GenResult2017()
}
