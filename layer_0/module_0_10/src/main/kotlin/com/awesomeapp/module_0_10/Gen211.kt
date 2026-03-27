package com.awesomeapp.module_0_10

data class GenModel211(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService211 {
    fun process(model: GenModel211): GenModel211
    fun validate(model: GenModel211): Boolean
}

class GenServiceImpl211 : GenService211 {
    override fun process(model: GenModel211): GenModel211 = model.copy(active = true)
    override fun validate(model: GenModel211): Boolean = model.name.isNotEmpty()
}

sealed class GenResult211 {
    data class Success(val data: GenModel211) : GenResult211()
    data class Error(val message: String) : GenResult211()
    data object Loading : GenResult211()
}
