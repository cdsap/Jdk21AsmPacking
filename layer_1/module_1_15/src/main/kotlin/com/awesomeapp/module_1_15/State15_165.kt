package com.awesomeapp.module_1_15

sealed class State15_165 {
    data object Loading : State15_165()
    data class Success(val data: String) : State15_165()
    data class Error(val message: String) : State15_165()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}