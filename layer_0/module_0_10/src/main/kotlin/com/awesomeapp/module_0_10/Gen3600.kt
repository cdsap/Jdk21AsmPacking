package com.awesomeapp.module_0_10

data class GenModel3600(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3600 {
    fun process(model: GenModel3600): GenModel3600
    fun validate(model: GenModel3600): Boolean
}

class GenServiceImpl3600 : GenService3600 {
    override fun process(model: GenModel3600): GenModel3600 = model.copy(active = true)
    override fun validate(model: GenModel3600): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3600 {
    data class Success(val data: GenModel3600) : GenResult3600()
    data class Error(val message: String) : GenResult3600()
    data object Loading : GenResult3600()
}
