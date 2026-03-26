package com.awesomeapp.module_0_1

sealed class State1_180 {
    data object Loading : State1_180()
    data class Success(val data: String) : State1_180()
    data class Error(val message: String) : State1_180()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}