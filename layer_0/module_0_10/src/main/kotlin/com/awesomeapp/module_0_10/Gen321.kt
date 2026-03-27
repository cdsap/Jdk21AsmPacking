package com.awesomeapp.module_0_10

data class GenModel321(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService321 {
    fun process(model: GenModel321): GenModel321
    fun validate(model: GenModel321): Boolean
}

class GenServiceImpl321 : GenService321 {
    override fun process(model: GenModel321): GenModel321 = model.copy(active = true)
    override fun validate(model: GenModel321): Boolean = model.name.isNotEmpty()
}

sealed class GenResult321 {
    data class Success(val data: GenModel321) : GenResult321()
    data class Error(val message: String) : GenResult321()
    data object Loading : GenResult321()
}
