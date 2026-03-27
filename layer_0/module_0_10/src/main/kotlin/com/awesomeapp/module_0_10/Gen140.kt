package com.awesomeapp.module_0_10

data class GenModel140(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService140 {
    fun process(model: GenModel140): GenModel140
    fun validate(model: GenModel140): Boolean
}

class GenServiceImpl140 : GenService140 {
    override fun process(model: GenModel140): GenModel140 = model.copy(active = true)
    override fun validate(model: GenModel140): Boolean = model.name.isNotEmpty()
}

sealed class GenResult140 {
    data class Success(val data: GenModel140) : GenResult140()
    data class Error(val message: String) : GenResult140()
    data object Loading : GenResult140()
}
