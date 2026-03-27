package com.awesomeapp.module_0_10

data class GenModel410(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService410 {
    fun process(model: GenModel410): GenModel410
    fun validate(model: GenModel410): Boolean
}

class GenServiceImpl410 : GenService410 {
    override fun process(model: GenModel410): GenModel410 = model.copy(active = true)
    override fun validate(model: GenModel410): Boolean = model.name.isNotEmpty()
}

sealed class GenResult410 {
    data class Success(val data: GenModel410) : GenResult410()
    data class Error(val message: String) : GenResult410()
    data object Loading : GenResult410()
}
