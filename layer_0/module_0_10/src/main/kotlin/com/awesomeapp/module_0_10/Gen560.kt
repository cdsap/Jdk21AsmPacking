package com.awesomeapp.module_0_10

data class GenModel560(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService560 {
    fun process(model: GenModel560): GenModel560
    fun validate(model: GenModel560): Boolean
}

class GenServiceImpl560 : GenService560 {
    override fun process(model: GenModel560): GenModel560 = model.copy(active = true)
    override fun validate(model: GenModel560): Boolean = model.name.isNotEmpty()
}

sealed class GenResult560 {
    data class Success(val data: GenModel560) : GenResult560()
    data class Error(val message: String) : GenResult560()
    data object Loading : GenResult560()
}
