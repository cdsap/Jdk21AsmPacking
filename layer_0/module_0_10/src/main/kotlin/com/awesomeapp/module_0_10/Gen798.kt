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

data class GenModel_798_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_798_ {
    data class Load(val id: Long) : GenEvent_798_()
    data class Update(val model: GenModel_798_) : GenEvent_798_()
    data class Delete(val id: Long) : GenEvent_798_()
    data object Refresh : GenEvent_798_()
    data class Search(val query: String) : GenEvent_798_()
    data class Filter(val predicate: String) : GenEvent_798_()
}

sealed class GenState_798_ {
    data object Idle : GenState_798_()
    data object Loading : GenState_798_()
    data class Success(val items: List<GenModel_798_>) : GenState_798_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_798_()
    data class Partial(val items: List<GenModel_798_>, val hasMore: Boolean) : GenState_798_()
}

interface GenRepository_798_ {
    suspend fun getAll(): List<GenModel_798_>
    suspend fun getById(id: Long): GenModel_798_?
    suspend fun save(model: GenModel_798_): GenModel_798_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_798_>
}

@Singleton
class GenRepositoryImpl_798_ @Inject constructor() : GenRepository_798_ {
    private val store = mutableMapOf<Long, GenModel_798_>()
    override suspend fun getAll(): List<GenModel_798_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_798_? = store[id]
    override suspend fun save(model: GenModel_798_): GenModel_798_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_798_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_798_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_798_ @Inject constructor(
    private val repository: GenRepositoryImpl_798_
) : GenUseCase_798_<Unit, List<GenModel_798_>> {
    override suspend fun invoke(params: Unit): List<GenModel_798_> = repository.getAll()
}

class GenSaveUseCase_798_ @Inject constructor(
    private val repository: GenRepositoryImpl_798_
) : GenUseCase_798_<GenModel_798_, GenModel_798_> {
    override suspend fun invoke(params: GenModel_798_): GenModel_798_ = repository.save(params)
}

class GenDeleteUseCase_798_ @Inject constructor(
    private val repository: GenRepositoryImpl_798_
) : GenUseCase_798_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_798_ @Inject constructor(
    private val repository: GenRepositoryImpl_798_
) : GenUseCase_798_<String, List<GenModel_798_>> {
    override suspend fun invoke(params: String): List<GenModel_798_> = repository.search(params)
}

abstract class GenMapper_798_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_798_ : GenMapper_798_<GenModel_798_, String>() {
    override fun map(input: GenModel_798_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_798_ : GenMapper_798_<String, GenModel_798_>() {
    override fun map(input: String): GenModel_798_ {
        val parts = input.split(":")
        return GenModel_798_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_798_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_798_,
    private val saveUseCase: GenSaveUseCase_798_,
    private val deleteUseCase: GenDeleteUseCase_798_,
    private val searchUseCase: GenSearchUseCase_798_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_798_>(GenState_798_.Idle)
    val state: StateFlow<GenState_798_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_798_) {
        when (event) {
            is GenEvent_798_.Load -> loadAll()
            is GenEvent_798_.Update -> save(event.model)
            is GenEvent_798_.Delete -> delete(event.id)
            is GenEvent_798_.Refresh -> loadAll()
            is GenEvent_798_.Search -> search(event.query)
            is GenEvent_798_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_798_.Loading; _state.value = GenState_798_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_798_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_798_.Success(searchUseCase(query)) } }
}
