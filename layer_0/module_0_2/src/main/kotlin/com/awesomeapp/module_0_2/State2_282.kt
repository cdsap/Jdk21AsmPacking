package com.awesomeapp.module_0_2

sealed class State2_282 {
    data object Loading : State2_282()
    data class Success(val data: String) : State2_282()
    data class Error(val message: String) : State2_282()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}