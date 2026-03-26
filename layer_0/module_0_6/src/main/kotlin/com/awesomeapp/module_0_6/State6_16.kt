package com.awesomeapp.module_0_6

sealed class State6_16 {
    data object Loading : State6_16()
    data class Success(val data: String) : State6_16()
    data class Error(val message: String) : State6_16()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}