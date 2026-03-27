package com.awesomeapp.module_0_10

data class GenModel513(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService513 {
    fun process(model: GenModel513): GenModel513
    fun validate(model: GenModel513): Boolean
}

class GenServiceImpl513 : GenService513 {
    override fun process(model: GenModel513): GenModel513 = model.copy(active = true)
    override fun validate(model: GenModel513): Boolean = model.name.isNotEmpty()
}

sealed class GenResult513 {
    data class Success(val data: GenModel513) : GenResult513()
    data class Error(val message: String) : GenResult513()
    data object Loading : GenResult513()
}
