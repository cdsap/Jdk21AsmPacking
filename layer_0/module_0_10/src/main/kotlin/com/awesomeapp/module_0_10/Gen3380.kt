package com.awesomeapp.module_0_10

data class GenModel3380(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3380 {
    fun process(model: GenModel3380): GenModel3380
    fun validate(model: GenModel3380): Boolean
}

class GenServiceImpl3380 : GenService3380 {
    override fun process(model: GenModel3380): GenModel3380 = model.copy(active = true)
    override fun validate(model: GenModel3380): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3380 {
    data class Success(val data: GenModel3380) : GenResult3380()
    data class Error(val message: String) : GenResult3380()
    data object Loading : GenResult3380()
}
