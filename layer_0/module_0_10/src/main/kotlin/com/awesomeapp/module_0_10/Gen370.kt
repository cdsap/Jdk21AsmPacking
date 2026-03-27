package com.awesomeapp.module_0_10

data class GenModel370(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService370 {
    fun process(model: GenModel370): GenModel370
    fun validate(model: GenModel370): Boolean
}

class GenServiceImpl370 : GenService370 {
    override fun process(model: GenModel370): GenModel370 = model.copy(active = true)
    override fun validate(model: GenModel370): Boolean = model.name.isNotEmpty()
}

sealed class GenResult370 {
    data class Success(val data: GenModel370) : GenResult370()
    data class Error(val message: String) : GenResult370()
    data object Loading : GenResult370()
}
