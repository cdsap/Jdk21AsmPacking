package com.awesomeapp.module_0_10

data class GenModel37(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService37 {
    fun process(model: GenModel37): GenModel37
    fun validate(model: GenModel37): Boolean
}

class GenServiceImpl37 : GenService37 {
    override fun process(model: GenModel37): GenModel37 = model.copy(active = true)
    override fun validate(model: GenModel37): Boolean = model.name.isNotEmpty()
}

sealed class GenResult37 {
    data class Success(val data: GenModel37) : GenResult37()
    data class Error(val message: String) : GenResult37()
    data object Loading : GenResult37()
}
