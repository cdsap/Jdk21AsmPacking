package com.awesomeapp.module_0_6

sealed class State6_73 {
    data object Loading : State6_73()
    data class Success(val data: String) : State6_73()
    data class Error(val message: String) : State6_73()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}