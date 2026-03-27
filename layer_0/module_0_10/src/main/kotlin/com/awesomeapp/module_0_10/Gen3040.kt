package com.awesomeapp.module_0_10

data class GenModel3040(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3040 {
    fun process(model: GenModel3040): GenModel3040
    fun validate(model: GenModel3040): Boolean
}

class GenServiceImpl3040 : GenService3040 {
    override fun process(model: GenModel3040): GenModel3040 = model.copy(active = true)
    override fun validate(model: GenModel3040): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3040 {
    data class Success(val data: GenModel3040) : GenResult3040()
    data class Error(val message: String) : GenResult3040()
    data object Loading : GenResult3040()
}
