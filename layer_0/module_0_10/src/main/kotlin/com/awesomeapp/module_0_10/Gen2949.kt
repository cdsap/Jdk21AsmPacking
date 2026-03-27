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

data class GenModel_2949_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2949_ {
    data class Load(val id: Long) : GenEvent_2949_()
    data class Update(val model: GenModel_2949_) : GenEvent_2949_()
    data class Delete(val id: Long) : GenEvent_2949_()
    data object Refresh : GenEvent_2949_()
    data class Search(val query: String) : GenEvent_2949_()
    data class Filter(val predicate: String) : GenEvent_2949_()
}

sealed class GenState_2949_ {
    data object Idle : GenState_2949_()
    data object Loading : GenState_2949_()
    data class Success(val items: List<GenModel_2949_>) : GenState_2949_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2949_()
    data class Partial(val items: List<GenModel_2949_>, val hasMore: Boolean) : GenState_2949_()
}

interface GenRepository_2949_ {
    suspend fun getAll(): List<GenModel_2949_>
    suspend fun getById(id: Long): GenModel_2949_?
    suspend fun save(model: GenModel_2949_): GenModel_2949_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2949_>
}

@Singleton
class GenRepositoryImpl_2949_ @Inject constructor() : GenRepository_2949_ {
    private val store = mutableMapOf<Long, GenModel_2949_>()
    override suspend fun getAll(): List<GenModel_2949_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2949_? = store[id]
    override suspend fun save(model: GenModel_2949_): GenModel_2949_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2949_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2949_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2949_ @Inject constructor(
    private val repository: GenRepositoryImpl_2949_
) : GenUseCase_2949_<Unit, List<GenModel_2949_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2949_> = repository.getAll()
}

class GenSaveUseCase_2949_ @Inject constructor(
    private val repository: GenRepositoryImpl_2949_
) : GenUseCase_2949_<GenModel_2949_, GenModel_2949_> {
    override suspend fun invoke(params: GenModel_2949_): GenModel_2949_ = repository.save(params)
}

class GenDeleteUseCase_2949_ @Inject constructor(
    private val repository: GenRepositoryImpl_2949_
) : GenUseCase_2949_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2949_ @Inject constructor(
    private val repository: GenRepositoryImpl_2949_
) : GenUseCase_2949_<String, List<GenModel_2949_>> {
    override suspend fun invoke(params: String): List<GenModel_2949_> = repository.search(params)
}

abstract class GenMapper_2949_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2949_ : GenMapper_2949_<GenModel_2949_, String>() {
    override fun map(input: GenModel_2949_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2949_ : GenMapper_2949_<String, GenModel_2949_>() {
    override fun map(input: String): GenModel_2949_ {
        val parts = input.split(":")
        return GenModel_2949_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2949_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2949_,
    private val saveUseCase: GenSaveUseCase_2949_,
    private val deleteUseCase: GenDeleteUseCase_2949_,
    private val searchUseCase: GenSearchUseCase_2949_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2949_>(GenState_2949_.Idle)
    val state: StateFlow<GenState_2949_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2949_) {
        when (event) {
            is GenEvent_2949_.Load -> loadAll()
            is GenEvent_2949_.Update -> save(event.model)
            is GenEvent_2949_.Delete -> delete(event.id)
            is GenEvent_2949_.Refresh -> loadAll()
            is GenEvent_2949_.Search -> search(event.query)
            is GenEvent_2949_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2949_.Loading; _state.value = GenState_2949_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2949_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2949_.Success(searchUseCase(query)) } }
}
