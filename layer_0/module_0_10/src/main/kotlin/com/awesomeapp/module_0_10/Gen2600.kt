package com.awesomeapp.module_0_10

data class GenModel2600(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2600 {
    fun process(model: GenModel2600): GenModel2600
    fun validate(model: GenModel2600): Boolean
}

class GenServiceImpl2600 : GenService2600 {
    override fun process(model: GenModel2600): GenModel2600 = model.copy(active = true)
    override fun validate(model: GenModel2600): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2600 {
    data class Success(val data: GenModel2600) : GenResult2600()
    data class Error(val message: String) : GenResult2600()
    data object Loading : GenResult2600()
}
