package com.awesomeapp.module_0_10

data class GenModel2022(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2022 {
    fun process(model: GenModel2022): GenModel2022
    fun validate(model: GenModel2022): Boolean
}

class GenServiceImpl2022 : GenService2022 {
    override fun process(model: GenModel2022): GenModel2022 = model.copy(active = true)
    override fun validate(model: GenModel2022): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2022 {
    data class Success(val data: GenModel2022) : GenResult2022()
    data class Error(val message: String) : GenResult2022()
    data object Loading : GenResult2022()
}
