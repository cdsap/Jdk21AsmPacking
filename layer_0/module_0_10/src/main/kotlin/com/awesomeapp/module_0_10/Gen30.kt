package com.awesomeapp.module_0_10

data class GenModel30(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService30 {
    fun process(model: GenModel30): GenModel30
    fun validate(model: GenModel30): Boolean
}

class GenServiceImpl30 : GenService30 {
    override fun process(model: GenModel30): GenModel30 = model.copy(active = true)
    override fun validate(model: GenModel30): Boolean = model.name.isNotEmpty()
}

sealed class GenResult30 {
    data class Success(val data: GenModel30) : GenResult30()
    data class Error(val message: String) : GenResult30()
    data object Loading : GenResult30()
}
