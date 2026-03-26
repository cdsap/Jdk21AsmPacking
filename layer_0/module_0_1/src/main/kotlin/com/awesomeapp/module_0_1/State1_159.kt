package com.awesomeapp.module_0_1

sealed class State1_159 {
    data object Loading : State1_159()
    data class Success(val data: String) : State1_159()
    data class Error(val message: String) : State1_159()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}