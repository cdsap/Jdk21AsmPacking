package com.awesomeapp.module_0_10

data class GenModel2360(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2360 {
    fun process(model: GenModel2360): GenModel2360
    fun validate(model: GenModel2360): Boolean
}

class GenServiceImpl2360 : GenService2360 {
    override fun process(model: GenModel2360): GenModel2360 = model.copy(active = true)
    override fun validate(model: GenModel2360): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2360 {
    data class Success(val data: GenModel2360) : GenResult2360()
    data class Error(val message: String) : GenResult2360()
    data object Loading : GenResult2360()
}
