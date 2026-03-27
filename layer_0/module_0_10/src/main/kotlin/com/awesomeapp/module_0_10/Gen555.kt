package com.awesomeapp.module_0_10

data class GenModel555(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService555 {
    fun process(model: GenModel555): GenModel555
    fun validate(model: GenModel555): Boolean
}

class GenServiceImpl555 : GenService555 {
    override fun process(model: GenModel555): GenModel555 = model.copy(active = true)
    override fun validate(model: GenModel555): Boolean = model.name.isNotEmpty()
}

sealed class GenResult555 {
    data class Success(val data: GenModel555) : GenResult555()
    data class Error(val message: String) : GenResult555()
    data object Loading : GenResult555()
}
