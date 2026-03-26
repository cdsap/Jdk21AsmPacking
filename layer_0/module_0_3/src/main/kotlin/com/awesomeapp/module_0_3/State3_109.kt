package com.awesomeapp.module_0_3

sealed class State3_109 {
    data object Loading : State3_109()
    data class Success(val data: String) : State3_109()
    data class Error(val message: String) : State3_109()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}