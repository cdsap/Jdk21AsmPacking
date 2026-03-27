package com.awesomeapp.module_0_10

data class GenModel2160(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2160 {
    fun process(model: GenModel2160): GenModel2160
    fun validate(model: GenModel2160): Boolean
}

class GenServiceImpl2160 : GenService2160 {
    override fun process(model: GenModel2160): GenModel2160 = model.copy(active = true)
    override fun validate(model: GenModel2160): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2160 {
    data class Success(val data: GenModel2160) : GenResult2160()
    data class Error(val message: String) : GenResult2160()
    data object Loading : GenResult2160()
}
