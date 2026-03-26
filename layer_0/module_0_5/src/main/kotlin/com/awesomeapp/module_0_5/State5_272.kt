package com.awesomeapp.module_0_5

sealed class State5_272 {
    data object Loading : State5_272()
    data class Success(val data: String) : State5_272()
    data class Error(val message: String) : State5_272()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}