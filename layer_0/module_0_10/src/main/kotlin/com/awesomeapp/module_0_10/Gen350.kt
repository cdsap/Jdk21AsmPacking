package com.awesomeapp.module_0_10

data class GenModel350(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService350 {
    fun process(model: GenModel350): GenModel350
    fun validate(model: GenModel350): Boolean
}

class GenServiceImpl350 : GenService350 {
    override fun process(model: GenModel350): GenModel350 = model.copy(active = true)
    override fun validate(model: GenModel350): Boolean = model.name.isNotEmpty()
}

sealed class GenResult350 {
    data class Success(val data: GenModel350) : GenResult350()
    data class Error(val message: String) : GenResult350()
    data object Loading : GenResult350()
}
