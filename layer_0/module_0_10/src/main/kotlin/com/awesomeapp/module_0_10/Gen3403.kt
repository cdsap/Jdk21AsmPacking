package com.awesomeapp.module_0_10

data class GenModel3403(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3403 {
    fun process(model: GenModel3403): GenModel3403
    fun validate(model: GenModel3403): Boolean
}

class GenServiceImpl3403 : GenService3403 {
    override fun process(model: GenModel3403): GenModel3403 = model.copy(active = true)
    override fun validate(model: GenModel3403): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3403 {
    data class Success(val data: GenModel3403) : GenResult3403()
    data class Error(val message: String) : GenResult3403()
    data object Loading : GenResult3403()
}
