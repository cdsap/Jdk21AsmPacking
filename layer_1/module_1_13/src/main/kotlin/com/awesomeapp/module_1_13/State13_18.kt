package com.awesomeapp.module_1_13

sealed class State13_18 {
    data object Loading : State13_18()
    data class Success(val data: String) : State13_18()
    data class Error(val message: String) : State13_18()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}