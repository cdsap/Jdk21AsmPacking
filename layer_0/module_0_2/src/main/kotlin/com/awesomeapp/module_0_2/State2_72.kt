package com.awesomeapp.module_0_2

sealed class State2_72 {
    data object Loading : State2_72()
    data class Success(val data: String) : State2_72()
    data class Error(val message: String) : State2_72()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}