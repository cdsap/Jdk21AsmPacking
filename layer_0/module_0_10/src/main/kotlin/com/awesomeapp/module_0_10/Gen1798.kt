package com.awesomeapp.module_0_10

import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GenModel_1798_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1798_ {
    data class Load(val id: Long) : GenEvent_1798_()
    data class Update(val model: GenModel_1798_) : GenEvent_1798_()
    data class Delete(val id: Long) : GenEvent_1798_()
    data object Refresh : GenEvent_1798_()
    data class Search(val query: String) : GenEvent_1798_()
    data class Filter(val predicate: String) : GenEvent_1798_()
}

sealed class GenState_1798_ {
    data object Idle : GenState_1798_()
    data object Loading : GenState_1798_()
    data class Success(val items: List<GenModel_1798_>) : GenState_1798_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1798_()
    data class Partial(val items: List<GenModel_1798_>, val hasMore: Boolean) : GenState_1798_()
}

interface GenRepository_1798_ {
    suspend fun getAll(): List<GenModel_1798_>
    suspend fun getById(id: Long): GenModel_1798_?
    suspend fun save(model: GenModel_1798_): GenModel_1798_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1798_>
}

@Singleton
class GenRepositoryImpl_1798_ @Inject constructor() : GenRepository_1798_ {
    private val store = mutableMapOf<Long, GenModel_1798_>()
    override suspend fun getAll(): List<GenModel_1798_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1798_? = store[id]
    override suspend fun save(model: GenModel_1798_): GenModel_1798_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1798_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1798_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1798_ @Inject constructor(
    private val repository: GenRepositoryImpl_1798_
) : GenUseCase_1798_<Unit, List<GenModel_1798_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1798_> = repository.getAll()
}

class GenSaveUseCase_1798_ @Inject constructor(
    private val repository: GenRepositoryImpl_1798_
) : GenUseCase_1798_<GenModel_1798_, GenModel_1798_> {
    override suspend fun invoke(params: GenModel_1798_): GenModel_1798_ = repository.save(params)
}

class GenDeleteUseCase_1798_ @Inject constructor(
    private val repository: GenRepositoryImpl_1798_
) : GenUseCase_1798_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1798_ @Inject constructor(
    private val repository: GenRepositoryImpl_1798_
) : GenUseCase_1798_<String, List<GenModel_1798_>> {
    override suspend fun invoke(params: String): List<GenModel_1798_> = repository.search(params)
}

abstract class GenMapper_1798_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1798_ : GenMapper_1798_<GenModel_1798_, String>() {
    override fun map(input: GenModel_1798_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1798_ : GenMapper_1798_<String, GenModel_1798_>() {
    override fun map(input: String): GenModel_1798_ {
        val parts = input.split(":")
        return GenModel_1798_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1798_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1798_,
    private val saveUseCase: GenSaveUseCase_1798_,
    private val deleteUseCase: GenDeleteUseCase_1798_,
    private val searchUseCase: GenSearchUseCase_1798_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1798_>(GenState_1798_.Idle)
    val state: StateFlow<GenState_1798_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1798_) {
        when (event) {
            is GenEvent_1798_.Load -> loadAll()
            is GenEvent_1798_.Update -> save(event.model)
            is GenEvent_1798_.Delete -> delete(event.id)
            is GenEvent_1798_.Refresh -> loadAll()
            is GenEvent_1798_.Search -> search(event.query)
            is GenEvent_1798_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1798_.Loading; _state.value = GenState_1798_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1798_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1798_.Success(searchUseCase(query)) } }
}
