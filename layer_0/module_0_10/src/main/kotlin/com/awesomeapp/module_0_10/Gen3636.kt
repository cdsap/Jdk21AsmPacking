package com.awesomeapp.module_0_10

data class GenModel3636(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3636 {
    fun process(model: GenModel3636): GenModel3636
    fun validate(model: GenModel3636): Boolean
}

class GenServiceImpl3636 : GenService3636 {
    override fun process(model: GenModel3636): GenModel3636 = model.copy(active = true)
    override fun validate(model: GenModel3636): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3636 {
    data class Success(val data: GenModel3636) : GenResult3636()
    data class Error(val message: String) : GenResult3636()
    data object Loading : GenResult3636()
}
