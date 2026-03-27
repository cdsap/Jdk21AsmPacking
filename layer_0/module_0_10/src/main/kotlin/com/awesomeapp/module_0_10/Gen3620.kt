package com.awesomeapp.module_0_10

data class GenModel3620(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3620 {
    fun process(model: GenModel3620): GenModel3620
    fun validate(model: GenModel3620): Boolean
}

class GenServiceImpl3620 : GenService3620 {
    override fun process(model: GenModel3620): GenModel3620 = model.copy(active = true)
    override fun validate(model: GenModel3620): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3620 {
    data class Success(val data: GenModel3620) : GenResult3620()
    data class Error(val message: String) : GenResult3620()
    data object Loading : GenResult3620()
}
