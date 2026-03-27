package com.awesomeapp.module_0_10

data class GenModel2110(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2110 {
    fun process(model: GenModel2110): GenModel2110
    fun validate(model: GenModel2110): Boolean
}

class GenServiceImpl2110 : GenService2110 {
    override fun process(model: GenModel2110): GenModel2110 = model.copy(active = true)
    override fun validate(model: GenModel2110): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2110 {
    data class Success(val data: GenModel2110) : GenResult2110()
    data class Error(val message: String) : GenResult2110()
    data object Loading : GenResult2110()
}
