package com.awesomeapp.module_1_12

sealed class State12_261 {
    data object Loading : State12_261()
    data class Success(val data: String) : State12_261()
    data class Error(val message: String) : State12_261()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}