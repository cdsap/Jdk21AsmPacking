package com.awesomeapp.module_3_19

sealed class State19_153 {
    data object Loading : State19_153()
    data class Success(val data: String) : State19_153()
    data class Error(val message: String) : State19_153()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}