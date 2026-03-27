package com.awesomeapp.module_0_10

data class GenModel2121(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2121 {
    fun process(model: GenModel2121): GenModel2121
    fun validate(model: GenModel2121): Boolean
}

class GenServiceImpl2121 : GenService2121 {
    override fun process(model: GenModel2121): GenModel2121 = model.copy(active = true)
    override fun validate(model: GenModel2121): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2121 {
    data class Success(val data: GenModel2121) : GenResult2121()
    data class Error(val message: String) : GenResult2121()
    data object Loading : GenResult2121()
}
