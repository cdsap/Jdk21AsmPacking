package com.awesomeapp.module_3_19

sealed class State19_27 {
    data object Loading : State19_27()
    data class Success(val data: String) : State19_27()
    data class Error(val message: String) : State19_27()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}