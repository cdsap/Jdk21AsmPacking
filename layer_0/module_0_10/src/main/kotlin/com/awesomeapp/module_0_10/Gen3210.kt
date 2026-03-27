package com.awesomeapp.module_0_10

data class GenModel3210(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3210 {
    fun process(model: GenModel3210): GenModel3210
    fun validate(model: GenModel3210): Boolean
}

class GenServiceImpl3210 : GenService3210 {
    override fun process(model: GenModel3210): GenModel3210 = model.copy(active = true)
    override fun validate(model: GenModel3210): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3210 {
    data class Success(val data: GenModel3210) : GenResult3210()
    data class Error(val message: String) : GenResult3210()
    data object Loading : GenResult3210()
}
