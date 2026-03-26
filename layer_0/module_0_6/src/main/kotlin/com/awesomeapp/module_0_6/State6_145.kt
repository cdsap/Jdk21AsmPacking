package com.awesomeapp.module_0_6

sealed class State6_145 {
    data object Loading : State6_145()
    data class Success(val data: String) : State6_145()
    data class Error(val message: String) : State6_145()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}