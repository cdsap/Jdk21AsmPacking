package com.awesomeapp.module_0_10

data class GenModel105(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService105 {
    fun process(model: GenModel105): GenModel105
    fun validate(model: GenModel105): Boolean
}

class GenServiceImpl105 : GenService105 {
    override fun process(model: GenModel105): GenModel105 = model.copy(active = true)
    override fun validate(model: GenModel105): Boolean = model.name.isNotEmpty()
}

sealed class GenResult105 {
    data class Success(val data: GenModel105) : GenResult105()
    data class Error(val message: String) : GenResult105()
    data object Loading : GenResult105()
}
