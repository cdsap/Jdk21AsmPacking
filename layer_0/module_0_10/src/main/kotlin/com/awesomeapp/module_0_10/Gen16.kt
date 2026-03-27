package com.awesomeapp.module_0_10

data class GenModel16(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService16 {
    fun process(model: GenModel16): GenModel16
    fun validate(model: GenModel16): Boolean
}

class GenServiceImpl16 : GenService16 {
    override fun process(model: GenModel16): GenModel16 = model.copy(active = true)
    override fun validate(model: GenModel16): Boolean = model.name.isNotEmpty()
}

sealed class GenResult16 {
    data class Success(val data: GenModel16) : GenResult16()
    data class Error(val message: String) : GenResult16()
    data object Loading : GenResult16()
}
