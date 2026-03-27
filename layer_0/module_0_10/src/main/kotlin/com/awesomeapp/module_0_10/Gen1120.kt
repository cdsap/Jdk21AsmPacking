package com.awesomeapp.module_0_10

data class GenModel1120(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1120 {
    fun process(model: GenModel1120): GenModel1120
    fun validate(model: GenModel1120): Boolean
}

class GenServiceImpl1120 : GenService1120 {
    override fun process(model: GenModel1120): GenModel1120 = model.copy(active = true)
    override fun validate(model: GenModel1120): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1120 {
    data class Success(val data: GenModel1120) : GenResult1120()
    data class Error(val message: String) : GenResult1120()
    data object Loading : GenResult1120()
}
