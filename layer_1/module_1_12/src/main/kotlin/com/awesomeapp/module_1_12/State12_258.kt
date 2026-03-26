package com.awesomeapp.module_1_12

sealed class State12_258 {
    data object Loading : State12_258()
    data class Success(val data: String) : State12_258()
    data class Error(val message: String) : State12_258()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}