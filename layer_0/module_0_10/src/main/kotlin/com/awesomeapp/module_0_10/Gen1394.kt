package com.awesomeapp.module_0_10

data class GenModel1394(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService1394 {
    fun process(model: GenModel1394): GenModel1394
    fun validate(model: GenModel1394): Boolean
}

class GenServiceImpl1394 : GenService1394 {
    override fun process(model: GenModel1394): GenModel1394 = model.copy(active = true)
    override fun validate(model: GenModel1394): Boolean = model.name.isNotEmpty()
}

sealed class GenResult1394 {
    data class Success(val data: GenModel1394) : GenResult1394()
    data class Error(val message: String) : GenResult1394()
    data object Loading : GenResult1394()
}
