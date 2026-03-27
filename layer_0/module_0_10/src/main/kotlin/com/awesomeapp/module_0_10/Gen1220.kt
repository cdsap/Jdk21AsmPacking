package com.awesomeapp.module_0_10

data class GenModel1220(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1220 {
    fun process(model: GenModel1220): GenModel1220
    fun validate(model: GenModel1220): Boolean
}

class GenServiceImpl1220 : GenService1220 {
    override fun process(model: GenModel1220): GenModel1220 = model.copy(active = true)
    override fun validate(model: GenModel1220): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1220 {
    data class Success(val data: GenModel1220) : GenResult1220()
    data class Error(val message: String) : GenResult1220()
    data object Loading : GenResult1220()
}
