package com.awesomeapp.module_0_10

data class GenModel850(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService850 {
    fun process(model: GenModel850): GenModel850
    fun validate(model: GenModel850): Boolean
}

class GenServiceImpl850 : GenService850 {
    override fun process(model: GenModel850): GenModel850 = model.copy(active = true)
    override fun validate(model: GenModel850): Boolean = model.name.isNotEmpty()
}

sealed class GenResult850 {
    data class Success(val data: GenModel850) : GenResult850()
    data class Error(val message: String) : GenResult850()
    data object Loading : GenResult850()
}
