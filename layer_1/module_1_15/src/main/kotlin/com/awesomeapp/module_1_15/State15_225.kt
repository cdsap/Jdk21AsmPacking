package com.awesomeapp.module_1_15

sealed class State15_225 {
    data object Loading : State15_225()
    data class Success(val data: String) : State15_225()
    data class Error(val message: String) : State15_225()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}