package com.awesomeapp.module_5_22

sealed class State22_126 {
    data object Loading : State22_126()
    data class Success(val data: String) : State22_126()
    data class Error(val message: String) : State22_126()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}