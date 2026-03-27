package com.awesomeapp.module_0_10

data class GenModel315(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService315 {
    fun process(model: GenModel315): GenModel315
    fun validate(model: GenModel315): Boolean
}

class GenServiceImpl315 : GenService315 {
    override fun process(model: GenModel315): GenModel315 = model.copy(active = true)
    override fun validate(model: GenModel315): Boolean = model.name.isNotEmpty()
}

sealed class GenResult315 {
    data class Success(val data: GenModel315) : GenResult315()
    data class Error(val message: String) : GenResult315()
    data object Loading : GenResult315()
}
