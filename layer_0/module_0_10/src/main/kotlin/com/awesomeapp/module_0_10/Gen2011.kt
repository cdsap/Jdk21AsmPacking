package com.awesomeapp.module_0_10

data class GenModel2011(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2011 {
    fun process(model: GenModel2011): GenModel2011
    fun validate(model: GenModel2011): Boolean
}

class GenServiceImpl2011 : GenService2011 {
    override fun process(model: GenModel2011): GenModel2011 = model.copy(active = true)
    override fun validate(model: GenModel2011): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2011 {
    data class Success(val data: GenModel2011) : GenResult2011()
    data class Error(val message: String) : GenResult2011()
    data object Loading : GenResult2011()
}
