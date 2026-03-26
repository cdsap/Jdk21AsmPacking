package com.awesomeapp.module_0_10

sealed class State10_257 {
    data object Loading : State10_257()
    data class Success(val data: String) : State10_257()
    data class Error(val message: String) : State10_257()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}