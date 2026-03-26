package com.awesomeapp.module_0_3

sealed class State3_151 {
    data object Loading : State3_151()
    data class Success(val data: String) : State3_151()
    data class Error(val message: String) : State3_151()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}