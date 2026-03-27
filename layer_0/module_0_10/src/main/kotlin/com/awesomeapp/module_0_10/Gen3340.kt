package com.awesomeapp.module_0_10

data class GenModel3340(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3340 {
    fun process(model: GenModel3340): GenModel3340
    fun validate(model: GenModel3340): Boolean
}

class GenServiceImpl3340 : GenService3340 {
    override fun process(model: GenModel3340): GenModel3340 = model.copy(active = true)
    override fun validate(model: GenModel3340): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3340 {
    data class Success(val data: GenModel3340) : GenResult3340()
    data class Error(val message: String) : GenResult3340()
    data object Loading : GenResult3340()
}
