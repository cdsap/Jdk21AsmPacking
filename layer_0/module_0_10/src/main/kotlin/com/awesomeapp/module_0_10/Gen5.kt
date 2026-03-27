package com.awesomeapp.module_0_10

data class GenModel5(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService5 {
    fun process(model: GenModel5): GenModel5
    fun validate(model: GenModel5): Boolean
}

class GenServiceImpl5 : GenService5 {
    override fun process(model: GenModel5): GenModel5 = model.copy(active = true)
    override fun validate(model: GenModel5): Boolean = model.name.isNotEmpty()
}

sealed class GenResult5 {
    data class Success(val data: GenModel5) : GenResult5()
    data class Error(val message: String) : GenResult5()
    data object Loading : GenResult5()
}
