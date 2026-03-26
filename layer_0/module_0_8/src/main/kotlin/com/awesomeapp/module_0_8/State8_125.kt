package com.awesomeapp.module_0_8

sealed class State8_125 {
    data object Loading : State8_125()
    data class Success(val data: String) : State8_125()
    data class Error(val message: String) : State8_125()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}