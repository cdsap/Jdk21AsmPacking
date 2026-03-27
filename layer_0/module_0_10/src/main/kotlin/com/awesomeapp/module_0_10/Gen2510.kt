package com.awesomeapp.module_0_10

data class GenModel2510(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2510 {
    fun process(model: GenModel2510): GenModel2510
    fun validate(model: GenModel2510): Boolean
}

class GenServiceImpl2510 : GenService2510 {
    override fun process(model: GenModel2510): GenModel2510 = model.copy(active = true)
    override fun validate(model: GenModel2510): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2510 {
    data class Success(val data: GenModel2510) : GenResult2510()
    data class Error(val message: String) : GenResult2510()
    data object Loading : GenResult2510()
}
