package com.awesomeapp.module_0_10

data class GenModel600(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService600 {
    fun process(model: GenModel600): GenModel600
    fun validate(model: GenModel600): Boolean
}

class GenServiceImpl600 : GenService600 {
    override fun process(model: GenModel600): GenModel600 = model.copy(active = true)
    override fun validate(model: GenModel600): Boolean = model.name.isNotEmpty()
}

sealed class GenResult600 {
    data class Success(val data: GenModel600) : GenResult600()
    data class Error(val message: String) : GenResult600()
    data object Loading : GenResult600()
}
