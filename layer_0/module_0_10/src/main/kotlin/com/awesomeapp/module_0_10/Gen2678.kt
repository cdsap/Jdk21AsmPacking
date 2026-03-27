package com.awesomeapp.module_0_10

data class GenModel2678(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2678 {
    fun process(model: GenModel2678): GenModel2678
    fun validate(model: GenModel2678): Boolean
}

class GenServiceImpl2678 : GenService2678 {
    override fun process(model: GenModel2678): GenModel2678 = model.copy(active = true)
    override fun validate(model: GenModel2678): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2678 {
    data class Success(val data: GenModel2678) : GenResult2678()
    data class Error(val message: String) : GenResult2678()
    data object Loading : GenResult2678()
}
