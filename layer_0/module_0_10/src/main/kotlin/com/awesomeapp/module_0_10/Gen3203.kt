package com.awesomeapp.module_0_10

data class GenModel3203(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3203 {
    fun process(model: GenModel3203): GenModel3203
    fun validate(model: GenModel3203): Boolean
}

class GenServiceImpl3203 : GenService3203 {
    override fun process(model: GenModel3203): GenModel3203 = model.copy(active = true)
    override fun validate(model: GenModel3203): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3203 {
    data class Success(val data: GenModel3203) : GenResult3203()
    data class Error(val message: String) : GenResult3203()
    data object Loading : GenResult3203()
}
