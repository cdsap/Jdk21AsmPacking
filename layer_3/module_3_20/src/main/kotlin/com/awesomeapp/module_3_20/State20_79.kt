package com.awesomeapp.module_3_20

sealed class State20_79 {
    data object Loading : State20_79()
    data class Success(val data: String) : State20_79()
    data class Error(val message: String) : State20_79()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}