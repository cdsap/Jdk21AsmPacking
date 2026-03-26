package com.awesomeapp.module_1_11

sealed class State11_267 {
    data object Loading : State11_267()
    data class Success(val data: String) : State11_267()
    data class Error(val message: String) : State11_267()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}