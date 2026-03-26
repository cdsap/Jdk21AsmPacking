package com.awesomeapp.module_3_20

sealed class State20_58 {
    data object Loading : State20_58()
    data class Success(val data: String) : State20_58()
    data class Error(val message: String) : State20_58()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}