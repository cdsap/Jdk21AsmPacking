package com.awesomeapp.module_0_10

data class GenModel5000(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService5000 {
    fun process(model: GenModel5000): GenModel5000
    fun validate(model: GenModel5000): Boolean
}

class GenServiceImpl5000 : GenService5000 {
    override fun process(model: GenModel5000): GenModel5000 = model.copy(active = true)
    override fun validate(model: GenModel5000): Boolean = model.name.isNotEmpty()
}

sealed class GenResult5000 {
    data class Success(val data: GenModel5000) : GenResult5000()
    data class Error(val message: String) : GenResult5000()
    data object Loading : GenResult5000()
}
