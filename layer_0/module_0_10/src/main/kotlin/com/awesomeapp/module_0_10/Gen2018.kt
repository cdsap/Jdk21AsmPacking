package com.awesomeapp.module_0_10

data class GenModel2018(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2018 {
    fun process(model: GenModel2018): GenModel2018
    fun validate(model: GenModel2018): Boolean
}

class GenServiceImpl2018 : GenService2018 {
    override fun process(model: GenModel2018): GenModel2018 = model.copy(active = true)
    override fun validate(model: GenModel2018): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2018 {
    data class Success(val data: GenModel2018) : GenResult2018()
    data class Error(val message: String) : GenResult2018()
    data object Loading : GenResult2018()
}
