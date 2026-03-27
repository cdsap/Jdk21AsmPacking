package com.awesomeapp.module_0_10

data class GenModel1231(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1231 {
    fun process(model: GenModel1231): GenModel1231
    fun validate(model: GenModel1231): Boolean
}

class GenServiceImpl1231 : GenService1231 {
    override fun process(model: GenModel1231): GenModel1231 = model.copy(active = true)
    override fun validate(model: GenModel1231): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1231 {
    data class Success(val data: GenModel1231) : GenResult1231()
    data class Error(val message: String) : GenResult1231()
    data object Loading : GenResult1231()
}
