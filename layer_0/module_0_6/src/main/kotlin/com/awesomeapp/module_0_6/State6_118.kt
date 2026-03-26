package com.awesomeapp.module_0_6

sealed class State6_118 {
    data object Loading : State6_118()
    data class Success(val data: String) : State6_118()
    data class Error(val message: String) : State6_118()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}