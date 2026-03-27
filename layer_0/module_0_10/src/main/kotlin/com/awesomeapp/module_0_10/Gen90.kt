package com.awesomeapp.module_0_10

data class GenModel90(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService90 {
    fun process(model: GenModel90): GenModel90
    fun validate(model: GenModel90): Boolean
}

class GenServiceImpl90 : GenService90 {
    override fun process(model: GenModel90): GenModel90 = model.copy(active = true)
    override fun validate(model: GenModel90): Boolean = model.name.isNotEmpty()
}

sealed class GenResult90 {
    data class Success(val data: GenModel90) : GenResult90()
    data class Error(val message: String) : GenResult90()
    data object Loading : GenResult90()
}
