package com.awesomeapp.module_0_10

data class GenModel2020(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2020 {
    fun process(model: GenModel2020): GenModel2020
    fun validate(model: GenModel2020): Boolean
}

class GenServiceImpl2020 : GenService2020 {
    override fun process(model: GenModel2020): GenModel2020 = model.copy(active = true)
    override fun validate(model: GenModel2020): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2020 {
    data class Success(val data: GenModel2020) : GenResult2020()
    data class Error(val message: String) : GenResult2020()
    data object Loading : GenResult2020()
}
