package com.awesomeapp.module_0_10

data class GenModel2512(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2512 {
    fun process(model: GenModel2512): GenModel2512
    fun validate(model: GenModel2512): Boolean
}

class GenServiceImpl2512 : GenService2512 {
    override fun process(model: GenModel2512): GenModel2512 = model.copy(active = true)
    override fun validate(model: GenModel2512): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2512 {
    data class Success(val data: GenModel2512) : GenResult2512()
    data class Error(val message: String) : GenResult2512()
    data object Loading : GenResult2512()
}
