package com.awesomeapp.module_0_10

data class GenModel2111(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2111 {
    fun process(model: GenModel2111): GenModel2111
    fun validate(model: GenModel2111): Boolean
}

class GenServiceImpl2111 : GenService2111 {
    override fun process(model: GenModel2111): GenModel2111 = model.copy(active = true)
    override fun validate(model: GenModel2111): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2111 {
    data class Success(val data: GenModel2111) : GenResult2111()
    data class Error(val message: String) : GenResult2111()
    data object Loading : GenResult2111()
}
