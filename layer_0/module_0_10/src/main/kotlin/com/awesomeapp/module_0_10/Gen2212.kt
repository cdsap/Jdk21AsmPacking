package com.awesomeapp.module_0_10

data class GenModel2212(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2212 {
    fun process(model: GenModel2212): GenModel2212
    fun validate(model: GenModel2212): Boolean
}

class GenServiceImpl2212 : GenService2212 {
    override fun process(model: GenModel2212): GenModel2212 = model.copy(active = true)
    override fun validate(model: GenModel2212): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2212 {
    data class Success(val data: GenModel2212) : GenResult2212()
    data class Error(val message: String) : GenResult2212()
    data object Loading : GenResult2212()
}
