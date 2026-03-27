package com.awesomeapp.module_0_10

data class GenModel220(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService220 {
    fun process(model: GenModel220): GenModel220
    fun validate(model: GenModel220): Boolean
}

class GenServiceImpl220 : GenService220 {
    override fun process(model: GenModel220): GenModel220 = model.copy(active = true)
    override fun validate(model: GenModel220): Boolean = model.name.isNotEmpty()
}

sealed class GenResult220 {
    data class Success(val data: GenModel220) : GenResult220()
    data class Error(val message: String) : GenResult220()
    data object Loading : GenResult220()
}
