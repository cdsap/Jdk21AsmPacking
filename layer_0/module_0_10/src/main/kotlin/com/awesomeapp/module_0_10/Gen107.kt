package com.awesomeapp.module_0_10

data class GenModel107(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService107 {
    fun process(model: GenModel107): GenModel107
    fun validate(model: GenModel107): Boolean
}

class GenServiceImpl107 : GenService107 {
    override fun process(model: GenModel107): GenModel107 = model.copy(active = true)
    override fun validate(model: GenModel107): Boolean = model.name.isNotEmpty()
}

sealed class GenResult107 {
    data class Success(val data: GenModel107) : GenResult107()
    data class Error(val message: String) : GenResult107()
    data object Loading : GenResult107()
}
