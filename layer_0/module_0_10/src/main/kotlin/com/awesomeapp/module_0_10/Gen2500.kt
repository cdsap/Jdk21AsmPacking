package com.awesomeapp.module_0_10

data class GenModel2500(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2500 {
    fun process(model: GenModel2500): GenModel2500
    fun validate(model: GenModel2500): Boolean
}

class GenServiceImpl2500 : GenService2500 {
    override fun process(model: GenModel2500): GenModel2500 = model.copy(active = true)
    override fun validate(model: GenModel2500): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2500 {
    data class Success(val data: GenModel2500) : GenResult2500()
    data class Error(val message: String) : GenResult2500()
    data object Loading : GenResult2500()
}
