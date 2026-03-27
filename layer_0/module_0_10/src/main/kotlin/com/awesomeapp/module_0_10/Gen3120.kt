package com.awesomeapp.module_0_10

data class GenModel3120(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3120 {
    fun process(model: GenModel3120): GenModel3120
    fun validate(model: GenModel3120): Boolean
}

class GenServiceImpl3120 : GenService3120 {
    override fun process(model: GenModel3120): GenModel3120 = model.copy(active = true)
    override fun validate(model: GenModel3120): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3120 {
    data class Success(val data: GenModel3120) : GenResult3120()
    data class Error(val message: String) : GenResult3120()
    data object Loading : GenResult3120()
}
