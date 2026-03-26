package com.awesomeapp.module_1_11

sealed class State11_78 {
    data object Loading : State11_78()
    data class Success(val data: String) : State11_78()
    data class Error(val message: String) : State11_78()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}