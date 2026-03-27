package com.awesomeapp.module_0_10

data class GenModel3857(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3857 {
    fun process(model: GenModel3857): GenModel3857
    fun validate(model: GenModel3857): Boolean
}

class GenServiceImpl3857 : GenService3857 {
    override fun process(model: GenModel3857): GenModel3857 = model.copy(active = true)
    override fun validate(model: GenModel3857): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3857 {
    data class Success(val data: GenModel3857) : GenResult3857()
    data class Error(val message: String) : GenResult3857()
    data object Loading : GenResult3857()
}
