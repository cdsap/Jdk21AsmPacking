package com.awesomeapp.module_0_10

data class GenModel3602(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3602 {
    fun process(model: GenModel3602): GenModel3602
    fun validate(model: GenModel3602): Boolean
}

class GenServiceImpl3602 : GenService3602 {
    override fun process(model: GenModel3602): GenModel3602 = model.copy(active = true)
    override fun validate(model: GenModel3602): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3602 {
    data class Success(val data: GenModel3602) : GenResult3602()
    data class Error(val message: String) : GenResult3602()
    data object Loading : GenResult3602()
}
