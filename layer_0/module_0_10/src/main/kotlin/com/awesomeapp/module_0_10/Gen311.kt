package com.awesomeapp.module_0_10

data class GenModel311(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService311 {
    fun process(model: GenModel311): GenModel311
    fun validate(model: GenModel311): Boolean
}

class GenServiceImpl311 : GenService311 {
    override fun process(model: GenModel311): GenModel311 = model.copy(active = true)
    override fun validate(model: GenModel311): Boolean = model.name.isNotEmpty()
}

sealed class GenResult311 {
    data class Success(val data: GenModel311) : GenResult311()
    data class Error(val message: String) : GenResult311()
    data object Loading : GenResult311()
}
