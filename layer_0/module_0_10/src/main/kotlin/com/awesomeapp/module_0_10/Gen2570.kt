package com.awesomeapp.module_0_10

data class GenModel2570(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2570 {
    fun process(model: GenModel2570): GenModel2570
    fun validate(model: GenModel2570): Boolean
}

class GenServiceImpl2570 : GenService2570 {
    override fun process(model: GenModel2570): GenModel2570 = model.copy(active = true)
    override fun validate(model: GenModel2570): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2570 {
    data class Success(val data: GenModel2570) : GenResult2570()
    data class Error(val message: String) : GenResult2570()
    data object Loading : GenResult2570()
}
