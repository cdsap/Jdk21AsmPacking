package com.awesomeapp.module_0_10

data class GenModel320(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService320 {
    fun process(model: GenModel320): GenModel320
    fun validate(model: GenModel320): Boolean
}

class GenServiceImpl320 : GenService320 {
    override fun process(model: GenModel320): GenModel320 = model.copy(active = true)
    override fun validate(model: GenModel320): Boolean = model.name.isNotEmpty()
}

sealed class GenResult320 {
    data class Success(val data: GenModel320) : GenResult320()
    data class Error(val message: String) : GenResult320()
    data object Loading : GenResult320()
}
