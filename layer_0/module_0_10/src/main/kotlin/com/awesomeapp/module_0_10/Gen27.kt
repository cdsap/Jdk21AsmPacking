package com.awesomeapp.module_0_10

data class GenModel27(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService27 {
    fun process(model: GenModel27): GenModel27
    fun validate(model: GenModel27): Boolean
}

class GenServiceImpl27 : GenService27 {
    override fun process(model: GenModel27): GenModel27 = model.copy(active = true)
    override fun validate(model: GenModel27): Boolean = model.name.isNotEmpty()
}

sealed class GenResult27 {
    data class Success(val data: GenModel27) : GenResult27()
    data class Error(val message: String) : GenResult27()
    data object Loading : GenResult27()
}
