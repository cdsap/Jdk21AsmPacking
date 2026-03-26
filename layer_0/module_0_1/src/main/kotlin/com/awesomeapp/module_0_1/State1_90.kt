package com.awesomeapp.module_0_1

sealed class State1_90 {
    data object Loading : State1_90()
    data class Success(val data: String) : State1_90()
    data class Error(val message: String) : State1_90()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}