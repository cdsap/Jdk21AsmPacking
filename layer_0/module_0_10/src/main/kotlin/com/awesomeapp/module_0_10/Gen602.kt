package com.awesomeapp.module_0_10

data class GenModel602(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService602 {
    fun process(model: GenModel602): GenModel602
    fun validate(model: GenModel602): Boolean
}

class GenServiceImpl602 : GenService602 {
    override fun process(model: GenModel602): GenModel602 = model.copy(active = true)
    override fun validate(model: GenModel602): Boolean = model.name.isNotEmpty()
}

sealed class GenResult602 {
    data class Success(val data: GenModel602) : GenResult602()
    data class Error(val message: String) : GenResult602()
    data object Loading : GenResult602()
}
