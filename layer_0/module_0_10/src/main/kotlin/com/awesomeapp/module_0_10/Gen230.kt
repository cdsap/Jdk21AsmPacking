package com.awesomeapp.module_0_10

data class GenModel230(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService230 {
    fun process(model: GenModel230): GenModel230
    fun validate(model: GenModel230): Boolean
}

class GenServiceImpl230 : GenService230 {
    override fun process(model: GenModel230): GenModel230 = model.copy(active = true)
    override fun validate(model: GenModel230): Boolean = model.name.isNotEmpty()
}

sealed class GenResult230 {
    data class Success(val data: GenModel230) : GenResult230()
    data class Error(val message: String) : GenResult230()
    data object Loading : GenResult230()
}
