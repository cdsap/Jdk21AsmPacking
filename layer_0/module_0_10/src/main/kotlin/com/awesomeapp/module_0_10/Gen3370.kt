package com.awesomeapp.module_0_10

data class GenModel3370(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3370 {
    fun process(model: GenModel3370): GenModel3370
    fun validate(model: GenModel3370): Boolean
}

class GenServiceImpl3370 : GenService3370 {
    override fun process(model: GenModel3370): GenModel3370 = model.copy(active = true)
    override fun validate(model: GenModel3370): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3370 {
    data class Success(val data: GenModel3370) : GenResult3370()
    data class Error(val message: String) : GenResult3370()
    data object Loading : GenResult3370()
}
