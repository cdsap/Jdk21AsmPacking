package com.awesomeapp.module_0_6

sealed class State6_55 {
    data object Loading : State6_55()
    data class Success(val data: String) : State6_55()
    data class Error(val message: String) : State6_55()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}