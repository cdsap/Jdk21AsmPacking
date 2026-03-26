package com.awesomeapp.module_0_2

sealed class State2_258 {
    data object Loading : State2_258()
    data class Success(val data: String) : State2_258()
    data class Error(val message: String) : State2_258()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}