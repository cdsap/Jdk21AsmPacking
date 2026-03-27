package com.awesomeapp.module_0_10

data class GenModel36(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService36 {
    fun process(model: GenModel36): GenModel36
    fun validate(model: GenModel36): Boolean
}

class GenServiceImpl36 : GenService36 {
    override fun process(model: GenModel36): GenModel36 = model.copy(active = true)
    override fun validate(model: GenModel36): Boolean = model.name.isNotEmpty()
}

sealed class GenResult36 {
    data class Success(val data: GenModel36) : GenResult36()
    data class Error(val message: String) : GenResult36()
    data object Loading : GenResult36()
}
