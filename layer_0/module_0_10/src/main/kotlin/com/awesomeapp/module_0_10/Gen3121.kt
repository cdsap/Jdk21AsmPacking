package com.awesomeapp.module_0_10

data class GenModel3121(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3121 {
    fun process(model: GenModel3121): GenModel3121
    fun validate(model: GenModel3121): Boolean
}

class GenServiceImpl3121 : GenService3121 {
    override fun process(model: GenModel3121): GenModel3121 = model.copy(active = true)
    override fun validate(model: GenModel3121): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3121 {
    data class Success(val data: GenModel3121) : GenResult3121()
    data class Error(val message: String) : GenResult3121()
    data object Loading : GenResult3121()
}
