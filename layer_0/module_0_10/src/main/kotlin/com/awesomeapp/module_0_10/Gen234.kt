package com.awesomeapp.module_0_10

data class GenModel234(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService234 {
    fun process(model: GenModel234): GenModel234
    fun validate(model: GenModel234): Boolean
}

class GenServiceImpl234 : GenService234 {
    override fun process(model: GenModel234): GenModel234 = model.copy(active = true)
    override fun validate(model: GenModel234): Boolean = model.name.isNotEmpty()
}

sealed class GenResult234 {
    data class Success(val data: GenModel234) : GenResult234()
    data class Error(val message: String) : GenResult234()
    data object Loading : GenResult234()
}
