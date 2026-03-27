package com.awesomeapp.module_0_10

data class GenModel403(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService403 {
    fun process(model: GenModel403): GenModel403
    fun validate(model: GenModel403): Boolean
}

class GenServiceImpl403 : GenService403 {
    override fun process(model: GenModel403): GenModel403 = model.copy(active = true)
    override fun validate(model: GenModel403): Boolean = model.name.isNotEmpty()
}

sealed class GenResult403 {
    data class Success(val data: GenModel403) : GenResult403()
    data class Error(val message: String) : GenResult403()
    data object Loading : GenResult403()
}
