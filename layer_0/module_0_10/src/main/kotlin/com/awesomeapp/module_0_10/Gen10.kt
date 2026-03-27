package com.awesomeapp.module_0_10

data class GenModel10(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService10 {
    fun process(model: GenModel10): GenModel10
    fun validate(model: GenModel10): Boolean
}

class GenServiceImpl10 : GenService10 {
    override fun process(model: GenModel10): GenModel10 = model.copy(active = true)
    override fun validate(model: GenModel10): Boolean = model.name.isNotEmpty()
}

sealed class GenResult10 {
    data class Success(val data: GenModel10) : GenResult10()
    data class Error(val message: String) : GenResult10()
    data object Loading : GenResult10()
}
