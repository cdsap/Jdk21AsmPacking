package com.awesomeapp.module_0_10

data class GenModel110(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService110 {
    fun process(model: GenModel110): GenModel110
    fun validate(model: GenModel110): Boolean
}

class GenServiceImpl110 : GenService110 {
    override fun process(model: GenModel110): GenModel110 = model.copy(active = true)
    override fun validate(model: GenModel110): Boolean = model.name.isNotEmpty()
}

sealed class GenResult110 {
    data class Success(val data: GenModel110) : GenResult110()
    data class Error(val message: String) : GenResult110()
    data object Loading : GenResult110()
}
