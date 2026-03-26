package com.awesomeapp.module_3_19

sealed class State19_261 {
    data object Loading : State19_261()
    data class Success(val data: String) : State19_261()
    data class Error(val message: String) : State19_261()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}