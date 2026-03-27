package com.awesomeapp.module_0_10

data class GenModel3273(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3273 {
    fun process(model: GenModel3273): GenModel3273
    fun validate(model: GenModel3273): Boolean
}

class GenServiceImpl3273 : GenService3273 {
    override fun process(model: GenModel3273): GenModel3273 = model.copy(active = true)
    override fun validate(model: GenModel3273): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3273 {
    data class Success(val data: GenModel3273) : GenResult3273()
    data class Error(val message: String) : GenResult3273()
    data object Loading : GenResult3273()
}
