package com.awesomeapp.module_0_10

data class GenModel4626(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4626 {
    fun process(model: GenModel4626): GenModel4626
    fun validate(model: GenModel4626): Boolean
}

class GenServiceImpl4626 : GenService4626 {
    override fun process(model: GenModel4626): GenModel4626 = model.copy(active = true)
    override fun validate(model: GenModel4626): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4626 {
    data class Success(val data: GenModel4626) : GenResult4626()
    data class Error(val message: String) : GenResult4626()
    data object Loading : GenResult4626()
}
