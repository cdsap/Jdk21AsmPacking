package com.awesomeapp.module_0_10

data class GenModel603(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService603 {
    fun process(model: GenModel603): GenModel603
    fun validate(model: GenModel603): Boolean
}

class GenServiceImpl603 : GenService603 {
    override fun process(model: GenModel603): GenModel603 = model.copy(active = true)
    override fun validate(model: GenModel603): Boolean = model.name.isNotEmpty()
}

sealed class GenResult603 {
    data class Success(val data: GenModel603) : GenResult603()
    data class Error(val message: String) : GenResult603()
    data object Loading : GenResult603()
}
