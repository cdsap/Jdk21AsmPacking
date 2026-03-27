package com.awesomeapp.module_0_10

data class GenModel12(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService12 {
    fun process(model: GenModel12): GenModel12
    fun validate(model: GenModel12): Boolean
}

class GenServiceImpl12 : GenService12 {
    override fun process(model: GenModel12): GenModel12 = model.copy(active = true)
    override fun validate(model: GenModel12): Boolean = model.name.isNotEmpty()
}

sealed class GenResult12 {
    data class Success(val data: GenModel12) : GenResult12()
    data class Error(val message: String) : GenResult12()
    data object Loading : GenResult12()
}
