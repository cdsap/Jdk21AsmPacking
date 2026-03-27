package com.awesomeapp.module_0_10

data class GenModel1105(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1105 {
    fun process(model: GenModel1105): GenModel1105
    fun validate(model: GenModel1105): Boolean
}

class GenServiceImpl1105 : GenService1105 {
    override fun process(model: GenModel1105): GenModel1105 = model.copy(active = true)
    override fun validate(model: GenModel1105): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1105 {
    data class Success(val data: GenModel1105) : GenResult1105()
    data class Error(val message: String) : GenResult1105()
    data object Loading : GenResult1105()
}
