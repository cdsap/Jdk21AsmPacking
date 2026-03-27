package com.awesomeapp.module_0_10

data class GenModel601(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService601 {
    fun process(model: GenModel601): GenModel601
    fun validate(model: GenModel601): Boolean
}

class GenServiceImpl601 : GenService601 {
    override fun process(model: GenModel601): GenModel601 = model.copy(active = true)
    override fun validate(model: GenModel601): Boolean = model.name.isNotEmpty()
}

sealed class GenResult601 {
    data class Success(val data: GenModel601) : GenResult601()
    data class Error(val message: String) : GenResult601()
    data object Loading : GenResult601()
}
