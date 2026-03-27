package com.awesomeapp.module_0_10

data class GenModel2333(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2333 {
    fun process(model: GenModel2333): GenModel2333
    fun validate(model: GenModel2333): Boolean
}

class GenServiceImpl2333 : GenService2333 {
    override fun process(model: GenModel2333): GenModel2333 = model.copy(active = true)
    override fun validate(model: GenModel2333): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2333 {
    data class Success(val data: GenModel2333) : GenResult2333()
    data class Error(val message: String) : GenResult2333()
    data object Loading : GenResult2333()
}
