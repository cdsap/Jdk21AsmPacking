package com.awesomeapp.module_0_10

data class GenModel121(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService121 {
    fun process(model: GenModel121): GenModel121
    fun validate(model: GenModel121): Boolean
}

class GenServiceImpl121 : GenService121 {
    override fun process(model: GenModel121): GenModel121 = model.copy(active = true)
    override fun validate(model: GenModel121): Boolean = model.name.isNotEmpty()
}

sealed class GenResult121 {
    data class Success(val data: GenModel121) : GenResult121()
    data class Error(val message: String) : GenResult121()
    data object Loading : GenResult121()
}
