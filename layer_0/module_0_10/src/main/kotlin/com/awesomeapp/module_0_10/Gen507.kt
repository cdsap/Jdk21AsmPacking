package com.awesomeapp.module_0_10

data class GenModel507(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService507 {
    fun process(model: GenModel507): GenModel507
    fun validate(model: GenModel507): Boolean
}

class GenServiceImpl507 : GenService507 {
    override fun process(model: GenModel507): GenModel507 = model.copy(active = true)
    override fun validate(model: GenModel507): Boolean = model.name.isNotEmpty()
}

sealed class GenResult507 {
    data class Success(val data: GenModel507) : GenResult507()
    data class Error(val message: String) : GenResult507()
    data object Loading : GenResult507()
}
