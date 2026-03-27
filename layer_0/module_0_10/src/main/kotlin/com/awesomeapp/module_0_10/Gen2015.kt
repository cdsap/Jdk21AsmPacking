package com.awesomeapp.module_0_10

data class GenModel2015(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2015 {
    fun process(model: GenModel2015): GenModel2015
    fun validate(model: GenModel2015): Boolean
}

class GenServiceImpl2015 : GenService2015 {
    override fun process(model: GenModel2015): GenModel2015 = model.copy(active = true)
    override fun validate(model: GenModel2015): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2015 {
    data class Success(val data: GenModel2015) : GenResult2015()
    data class Error(val message: String) : GenResult2015()
    data object Loading : GenResult2015()
}
