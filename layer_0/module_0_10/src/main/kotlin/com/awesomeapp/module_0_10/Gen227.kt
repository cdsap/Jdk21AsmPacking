package com.awesomeapp.module_0_10

data class GenModel227(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService227 {
    fun process(model: GenModel227): GenModel227
    fun validate(model: GenModel227): Boolean
}

class GenServiceImpl227 : GenService227 {
    override fun process(model: GenModel227): GenModel227 = model.copy(active = true)
    override fun validate(model: GenModel227): Boolean = model.name.isNotEmpty()
}

sealed class GenResult227 {
    data class Success(val data: GenModel227) : GenResult227()
    data class Error(val message: String) : GenResult227()
    data object Loading : GenResult227()
}
