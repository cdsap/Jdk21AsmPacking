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

data class GenModel_779_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_779_ {
    data class Load(val id: Long) : GenEvent_779_()
    data class Update(val model: GenModel_779_) : GenEvent_779_()
    data class Delete(val id: Long) : GenEvent_779_()
    data object Refresh : GenEvent_779_()
    data class Search(val query: String) : GenEvent_779_()
    data class Filter(val predicate: String) : GenEvent_779_()
}

sealed class GenState_779_ {
    data object Idle : GenState_779_()
    data object Loading : GenState_779_()
    data class Success(val items: List<GenModel_779_>) : GenState_779_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_779_()
    data class Partial(val items: List<GenModel_779_>, val hasMore: Boolean) : GenState_779_()
}

interface GenRepository_779_ {
    suspend fun getAll(): List<GenModel_779_>
    suspend fun getById(id: Long): GenModel_779_?
    suspend fun save(model: GenModel_779_): GenModel_779_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_779_>
}

@Singleton
class GenRepositoryImpl_779_ @Inject constructor() : GenRepository_779_ {
    private val store = mutableMapOf<Long, GenModel_779_>()
    override suspend fun getAll(): List<GenModel_779_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_779_? = store[id]
    override suspend fun save(model: GenModel_779_): GenModel_779_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_779_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_779_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_779_ @Inject constructor(
    private val repository: GenRepositoryImpl_779_
) : GenUseCase_779_<Unit, List<GenModel_779_>> {
    override suspend fun invoke(params: Unit): List<GenModel_779_> = repository.getAll()
}

class GenSaveUseCase_779_ @Inject constructor(
    private val repository: GenRepositoryImpl_779_
) : GenUseCase_779_<GenModel_779_, GenModel_779_> {
    override suspend fun invoke(params: GenModel_779_): GenModel_779_ = repository.save(params)
}

class GenDeleteUseCase_779_ @Inject constructor(
    private val repository: GenRepositoryImpl_779_
) : GenUseCase_779_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_779_ @Inject constructor(
    private val repository: GenRepositoryImpl_779_
) : GenUseCase_779_<String, List<GenModel_779_>> {
    override suspend fun invoke(params: String): List<GenModel_779_> = repository.search(params)
}

abstract class GenMapper_779_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_779_ : GenMapper_779_<GenModel_779_, String>() {
    override fun map(input: GenModel_779_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_779_ : GenMapper_779_<String, GenModel_779_>() {
    override fun map(input: String): GenModel_779_ {
        val parts = input.split(":")
        return GenModel_779_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_779_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_779_,
    private val saveUseCase: GenSaveUseCase_779_,
    private val deleteUseCase: GenDeleteUseCase_779_,
    private val searchUseCase: GenSearchUseCase_779_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_779_>(GenState_779_.Idle)
    val state: StateFlow<GenState_779_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_779_) {
        when (event) {
            is GenEvent_779_.Load -> loadAll()
            is GenEvent_779_.Update -> save(event.model)
            is GenEvent_779_.Delete -> delete(event.id)
            is GenEvent_779_.Refresh -> loadAll()
            is GenEvent_779_.Search -> search(event.query)
            is GenEvent_779_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_779_.Loading; _state.value = GenState_779_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_779_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_779_.Success(searchUseCase(query)) } }
}
