package com.awesomeapp.module_0_4

sealed class State4_188 {
    data object Loading : State4_188()
    data class Success(val data: String) : State4_188()
    data class Error(val message: String) : State4_188()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}