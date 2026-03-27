package com.awesomeapp.module_0_10

data class GenModel2560(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2560 {
    fun process(model: GenModel2560): GenModel2560
    fun validate(model: GenModel2560): Boolean
}

class GenServiceImpl2560 : GenService2560 {
    override fun process(model: GenModel2560): GenModel2560 = model.copy(active = true)
    override fun validate(model: GenModel2560): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2560 {
    data class Success(val data: GenModel2560) : GenResult2560()
    data class Error(val message: String) : GenResult2560()
    data object Loading : GenResult2560()
}
