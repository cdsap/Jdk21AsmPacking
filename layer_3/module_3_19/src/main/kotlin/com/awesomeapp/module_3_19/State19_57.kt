package com.awesomeapp.module_3_19

sealed class State19_57 {
    data object Loading : State19_57()
    data class Success(val data: String) : State19_57()
    data class Error(val message: String) : State19_57()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}