package com.awesomeapp.module_0_1

sealed class State1_18 {
    data object Loading : State1_18()
    data class Success(val data: String) : State1_18()
    data class Error(val message: String) : State1_18()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}