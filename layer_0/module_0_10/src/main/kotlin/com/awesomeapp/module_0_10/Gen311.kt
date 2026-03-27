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

data class GenModel_311_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_311_ {
    data class Load(val id: Long) : GenEvent_311_()
    data class Update(val model: GenModel_311_) : GenEvent_311_()
    data class Delete(val id: Long) : GenEvent_311_()
    data object Refresh : GenEvent_311_()
    data class Search(val query: String) : GenEvent_311_()
    data class Filter(val predicate: String) : GenEvent_311_()
}

sealed class GenState_311_ {
    data object Idle : GenState_311_()
    data object Loading : GenState_311_()
    data class Success(val items: List<GenModel_311_>) : GenState_311_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_311_()
    data class Partial(val items: List<GenModel_311_>, val hasMore: Boolean) : GenState_311_()
}

interface GenRepository_311_ {
    suspend fun getAll(): List<GenModel_311_>
    suspend fun getById(id: Long): GenModel_311_?
    suspend fun save(model: GenModel_311_): GenModel_311_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_311_>
}

@Singleton
class GenRepositoryImpl_311_ @Inject constructor() : GenRepository_311_ {
    private val store = mutableMapOf<Long, GenModel_311_>()
    override suspend fun getAll(): List<GenModel_311_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_311_? = store[id]
    override suspend fun save(model: GenModel_311_): GenModel_311_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_311_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_311_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_311_ @Inject constructor(
    private val repository: GenRepositoryImpl_311_
) : GenUseCase_311_<Unit, List<GenModel_311_>> {
    override suspend fun invoke(params: Unit): List<GenModel_311_> = repository.getAll()
}

class GenSaveUseCase_311_ @Inject constructor(
    private val repository: GenRepositoryImpl_311_
) : GenUseCase_311_<GenModel_311_, GenModel_311_> {
    override suspend fun invoke(params: GenModel_311_): GenModel_311_ = repository.save(params)
}

class GenDeleteUseCase_311_ @Inject constructor(
    private val repository: GenRepositoryImpl_311_
) : GenUseCase_311_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_311_ @Inject constructor(
    private val repository: GenRepositoryImpl_311_
) : GenUseCase_311_<String, List<GenModel_311_>> {
    override suspend fun invoke(params: String): List<GenModel_311_> = repository.search(params)
}

abstract class GenMapper_311_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_311_ : GenMapper_311_<GenModel_311_, String>() {
    override fun map(input: GenModel_311_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_311_ : GenMapper_311_<String, GenModel_311_>() {
    override fun map(input: String): GenModel_311_ {
        val parts = input.split(":")
        return GenModel_311_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_311_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_311_,
    private val saveUseCase: GenSaveUseCase_311_,
    private val deleteUseCase: GenDeleteUseCase_311_,
    private val searchUseCase: GenSearchUseCase_311_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_311_>(GenState_311_.Idle)
    val state: StateFlow<GenState_311_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_311_) {
        when (event) {
            is GenEvent_311_.Load -> loadAll()
            is GenEvent_311_.Update -> save(event.model)
            is GenEvent_311_.Delete -> delete(event.id)
            is GenEvent_311_.Refresh -> loadAll()
            is GenEvent_311_.Search -> search(event.query)
            is GenEvent_311_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_311_.Loading; _state.value = GenState_311_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_311_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_311_.Success(searchUseCase(query)) } }
}
