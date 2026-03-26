package com.awesomeapp.module_0_8

sealed class State8_164 {
    data object Loading : State8_164()
    data class Success(val data: String) : State8_164()
    data class Error(val message: String) : State8_164()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}