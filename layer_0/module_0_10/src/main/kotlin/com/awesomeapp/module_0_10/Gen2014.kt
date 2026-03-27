package com.awesomeapp.module_0_10

data class GenModel2014(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2014 {
    fun process(model: GenModel2014): GenModel2014
    fun validate(model: GenModel2014): Boolean
}

class GenServiceImpl2014 : GenService2014 {
    override fun process(model: GenModel2014): GenModel2014 = model.copy(active = true)
    override fun validate(model: GenModel2014): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2014 {
    data class Success(val data: GenModel2014) : GenResult2014()
    data class Error(val message: String) : GenResult2014()
    data object Loading : GenResult2014()
}
