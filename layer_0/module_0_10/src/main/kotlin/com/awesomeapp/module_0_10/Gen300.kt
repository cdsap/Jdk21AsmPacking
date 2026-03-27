package com.awesomeapp.module_0_10

data class GenModel300(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService300 {
    fun process(model: GenModel300): GenModel300
    fun validate(model: GenModel300): Boolean
}

class GenServiceImpl300 : GenService300 {
    override fun process(model: GenModel300): GenModel300 = model.copy(active = true)
    override fun validate(model: GenModel300): Boolean = model.name.isNotEmpty()
}

sealed class GenResult300 {
    data class Success(val data: GenModel300) : GenResult300()
    data class Error(val message: String) : GenResult300()
    data object Loading : GenResult300()
}
