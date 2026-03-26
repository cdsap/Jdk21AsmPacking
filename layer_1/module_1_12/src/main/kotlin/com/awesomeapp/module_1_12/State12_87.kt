package com.awesomeapp.module_1_12

sealed class State12_87 {
    data object Loading : State12_87()
    data class Success(val data: String) : State12_87()
    data class Error(val message: String) : State12_87()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}