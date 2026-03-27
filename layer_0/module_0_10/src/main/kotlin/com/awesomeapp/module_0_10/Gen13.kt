package com.awesomeapp.module_0_10

data class GenModel13(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService13 {
    fun process(model: GenModel13): GenModel13
    fun validate(model: GenModel13): Boolean
}

class GenServiceImpl13 : GenService13 {
    override fun process(model: GenModel13): GenModel13 = model.copy(active = true)
    override fun validate(model: GenModel13): Boolean = model.name.isNotEmpty()
}

sealed class GenResult13 {
    data class Success(val data: GenModel13) : GenResult13()
    data class Error(val message: String) : GenResult13()
    data object Loading : GenResult13()
}
