package com.awesomeapp.module_0_10

data class GenModel2016(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2016 {
    fun process(model: GenModel2016): GenModel2016
    fun validate(model: GenModel2016): Boolean
}

class GenServiceImpl2016 : GenService2016 {
    override fun process(model: GenModel2016): GenModel2016 = model.copy(active = true)
    override fun validate(model: GenModel2016): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2016 {
    data class Success(val data: GenModel2016) : GenResult2016()
    data class Error(val message: String) : GenResult2016()
    data object Loading : GenResult2016()
}
