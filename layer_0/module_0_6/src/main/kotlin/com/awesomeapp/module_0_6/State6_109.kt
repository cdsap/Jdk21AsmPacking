package com.awesomeapp.module_0_6

sealed class State6_109 {
    data object Loading : State6_109()
    data class Success(val data: String) : State6_109()
    data class Error(val message: String) : State6_109()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}