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

data class GenModel_953_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_953_ {
    data class Load(val id: Long) : GenEvent_953_()
    data class Update(val model: GenModel_953_) : GenEvent_953_()
    data class Delete(val id: Long) : GenEvent_953_()
    data object Refresh : GenEvent_953_()
    data class Search(val query: String) : GenEvent_953_()
    data class Filter(val predicate: String) : GenEvent_953_()
}

sealed class GenState_953_ {
    data object Idle : GenState_953_()
    data object Loading : GenState_953_()
    data class Success(val items: List<GenModel_953_>) : GenState_953_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_953_()
    data class Partial(val items: List<GenModel_953_>, val hasMore: Boolean) : GenState_953_()
}

interface GenRepository_953_ {
    suspend fun getAll(): List<GenModel_953_>
    suspend fun getById(id: Long): GenModel_953_?
    suspend fun save(model: GenModel_953_): GenModel_953_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_953_>
}

@Singleton
class GenRepositoryImpl_953_ @Inject constructor() : GenRepository_953_ {
    private val store = mutableMapOf<Long, GenModel_953_>()
    override suspend fun getAll(): List<GenModel_953_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_953_? = store[id]
    override suspend fun save(model: GenModel_953_): GenModel_953_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_953_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_953_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_953_ @Inject constructor(
    private val repository: GenRepositoryImpl_953_
) : GenUseCase_953_<Unit, List<GenModel_953_>> {
    override suspend fun invoke(params: Unit): List<GenModel_953_> = repository.getAll()
}

class GenSaveUseCase_953_ @Inject constructor(
    private val repository: GenRepositoryImpl_953_
) : GenUseCase_953_<GenModel_953_, GenModel_953_> {
    override suspend fun invoke(params: GenModel_953_): GenModel_953_ = repository.save(params)
}

class GenDeleteUseCase_953_ @Inject constructor(
    private val repository: GenRepositoryImpl_953_
) : GenUseCase_953_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_953_ @Inject constructor(
    private val repository: GenRepositoryImpl_953_
) : GenUseCase_953_<String, List<GenModel_953_>> {
    override suspend fun invoke(params: String): List<GenModel_953_> = repository.search(params)
}

abstract class GenMapper_953_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_953_ : GenMapper_953_<GenModel_953_, String>() {
    override fun map(input: GenModel_953_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_953_ : GenMapper_953_<String, GenModel_953_>() {
    override fun map(input: String): GenModel_953_ {
        val parts = input.split(":")
        return GenModel_953_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_953_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_953_,
    private val saveUseCase: GenSaveUseCase_953_,
    private val deleteUseCase: GenDeleteUseCase_953_,
    private val searchUseCase: GenSearchUseCase_953_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_953_>(GenState_953_.Idle)
    val state: StateFlow<GenState_953_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_953_) {
        when (event) {
            is GenEvent_953_.Load -> loadAll()
            is GenEvent_953_.Update -> save(event.model)
            is GenEvent_953_.Delete -> delete(event.id)
            is GenEvent_953_.Refresh -> loadAll()
            is GenEvent_953_.Search -> search(event.query)
            is GenEvent_953_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_953_.Loading; _state.value = GenState_953_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_953_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_953_.Success(searchUseCase(query)) } }
}
