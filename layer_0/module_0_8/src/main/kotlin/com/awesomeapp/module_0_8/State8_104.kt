package com.awesomeapp.module_0_8

sealed class State8_104 {
    data object Loading : State8_104()
    data class Success(val data: String) : State8_104()
    data class Error(val message: String) : State8_104()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}