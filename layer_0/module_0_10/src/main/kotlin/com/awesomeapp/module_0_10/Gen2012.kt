package com.awesomeapp.module_0_10

data class GenModel2012(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2012 {
    fun process(model: GenModel2012): GenModel2012
    fun validate(model: GenModel2012): Boolean
}

class GenServiceImpl2012 : GenService2012 {
    override fun process(model: GenModel2012): GenModel2012 = model.copy(active = true)
    override fun validate(model: GenModel2012): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2012 {
    data class Success(val data: GenModel2012) : GenResult2012()
    data class Error(val message: String) : GenResult2012()
    data object Loading : GenResult2012()
}
