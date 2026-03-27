package com.awesomeapp.module_0_10

data class GenModel120(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService120 {
    fun process(model: GenModel120): GenModel120
    fun validate(model: GenModel120): Boolean
}

class GenServiceImpl120 : GenService120 {
    override fun process(model: GenModel120): GenModel120 = model.copy(active = true)
    override fun validate(model: GenModel120): Boolean = model.name.isNotEmpty()
}

sealed class GenResult120 {
    data class Success(val data: GenModel120) : GenResult120()
    data class Error(val message: String) : GenResult120()
    data object Loading : GenResult120()
}
