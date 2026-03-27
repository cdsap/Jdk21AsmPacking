package com.awesomeapp.module_0_10

data class GenModel2021(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2021 {
    fun process(model: GenModel2021): GenModel2021
    fun validate(model: GenModel2021): Boolean
}

class GenServiceImpl2021 : GenService2021 {
    override fun process(model: GenModel2021): GenModel2021 = model.copy(active = true)
    override fun validate(model: GenModel2021): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2021 {
    data class Success(val data: GenModel2021) : GenResult2021()
    data class Error(val message: String) : GenResult2021()
    data object Loading : GenResult2021()
}
