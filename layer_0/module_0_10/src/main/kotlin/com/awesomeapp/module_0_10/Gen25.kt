package com.awesomeapp.module_0_10

data class GenModel25(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService25 {
    fun process(model: GenModel25): GenModel25
    fun validate(model: GenModel25): Boolean
}

class GenServiceImpl25 : GenService25 {
    override fun process(model: GenModel25): GenModel25 = model.copy(active = true)
    override fun validate(model: GenModel25): Boolean = model.name.isNotEmpty()
}

sealed class GenResult25 {
    data class Success(val data: GenModel25) : GenResult25()
    data class Error(val message: String) : GenResult25()
    data object Loading : GenResult25()
}
