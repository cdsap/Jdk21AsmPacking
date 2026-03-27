package com.awesomeapp.module_0_10

data class GenModel2120(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2120 {
    fun process(model: GenModel2120): GenModel2120
    fun validate(model: GenModel2120): Boolean
}

class GenServiceImpl2120 : GenService2120 {
    override fun process(model: GenModel2120): GenModel2120 = model.copy(active = true)
    override fun validate(model: GenModel2120): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2120 {
    data class Success(val data: GenModel2120) : GenResult2120()
    data class Error(val message: String) : GenResult2120()
    data object Loading : GenResult2120()
}
