package com.awesomeapp.module_0_10

data class GenModel3010(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3010 {
    fun process(model: GenModel3010): GenModel3010
    fun validate(model: GenModel3010): Boolean
}

class GenServiceImpl3010 : GenService3010 {
    override fun process(model: GenModel3010): GenModel3010 = model.copy(active = true)
    override fun validate(model: GenModel3010): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3010 {
    data class Success(val data: GenModel3010) : GenResult3010()
    data class Error(val message: String) : GenResult3010()
    data object Loading : GenResult3010()
}
