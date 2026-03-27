package com.awesomeapp.module_0_10

data class GenModel802(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService802 {
    fun process(model: GenModel802): GenModel802
    fun validate(model: GenModel802): Boolean
}

class GenServiceImpl802 : GenService802 {
    override fun process(model: GenModel802): GenModel802 = model.copy(active = true)
    override fun validate(model: GenModel802): Boolean = model.name.isNotEmpty()
}

sealed class GenResult802 {
    data class Success(val data: GenModel802) : GenResult802()
    data class Error(val message: String) : GenResult802()
    data object Loading : GenResult802()
}
