package com.awesomeapp.module_1_11

sealed class State11_156 {
    data object Loading : State11_156()
    data class Success(val data: String) : State11_156()
    data class Error(val message: String) : State11_156()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}