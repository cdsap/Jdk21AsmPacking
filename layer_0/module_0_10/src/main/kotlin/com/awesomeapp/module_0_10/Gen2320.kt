package com.awesomeapp.module_0_10

data class GenModel2320(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2320 {
    fun process(model: GenModel2320): GenModel2320
    fun validate(model: GenModel2320): Boolean
}

class GenServiceImpl2320 : GenService2320 {
    override fun process(model: GenModel2320): GenModel2320 = model.copy(active = true)
    override fun validate(model: GenModel2320): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2320 {
    data class Success(val data: GenModel2320) : GenResult2320()
    data class Error(val message: String) : GenResult2320()
    data object Loading : GenResult2320()
}
