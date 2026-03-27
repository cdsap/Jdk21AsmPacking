package com.awesomeapp.module_0_10

data class GenModel3300(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3300 {
    fun process(model: GenModel3300): GenModel3300
    fun validate(model: GenModel3300): Boolean
}

class GenServiceImpl3300 : GenService3300 {
    override fun process(model: GenModel3300): GenModel3300 = model.copy(active = true)
    override fun validate(model: GenModel3300): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3300 {
    data class Success(val data: GenModel3300) : GenResult3300()
    data class Error(val message: String) : GenResult3300()
    data object Loading : GenResult3300()
}
