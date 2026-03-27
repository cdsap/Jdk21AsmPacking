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

data class GenModel_884_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_884_ {
    data class Load(val id: Long) : GenEvent_884_()
    data class Update(val model: GenModel_884_) : GenEvent_884_()
    data class Delete(val id: Long) : GenEvent_884_()
    data object Refresh : GenEvent_884_()
    data class Search(val query: String) : GenEvent_884_()
    data class Filter(val predicate: String) : GenEvent_884_()
}

sealed class GenState_884_ {
    data object Idle : GenState_884_()
    data object Loading : GenState_884_()
    data class Success(val items: List<GenModel_884_>) : GenState_884_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_884_()
    data class Partial(val items: List<GenModel_884_>, val hasMore: Boolean) : GenState_884_()
}

interface GenRepository_884_ {
    suspend fun getAll(): List<GenModel_884_>
    suspend fun getById(id: Long): GenModel_884_?
    suspend fun save(model: GenModel_884_): GenModel_884_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_884_>
}

@Singleton
class GenRepositoryImpl_884_ @Inject constructor() : GenRepository_884_ {
    private val store = mutableMapOf<Long, GenModel_884_>()
    override suspend fun getAll(): List<GenModel_884_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_884_? = store[id]
    override suspend fun save(model: GenModel_884_): GenModel_884_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_884_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_884_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_884_ @Inject constructor(
    private val repository: GenRepositoryImpl_884_
) : GenUseCase_884_<Unit, List<GenModel_884_>> {
    override suspend fun invoke(params: Unit): List<GenModel_884_> = repository.getAll()
}

class GenSaveUseCase_884_ @Inject constructor(
    private val repository: GenRepositoryImpl_884_
) : GenUseCase_884_<GenModel_884_, GenModel_884_> {
    override suspend fun invoke(params: GenModel_884_): GenModel_884_ = repository.save(params)
}

class GenDeleteUseCase_884_ @Inject constructor(
    private val repository: GenRepositoryImpl_884_
) : GenUseCase_884_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_884_ @Inject constructor(
    private val repository: GenRepositoryImpl_884_
) : GenUseCase_884_<String, List<GenModel_884_>> {
    override suspend fun invoke(params: String): List<GenModel_884_> = repository.search(params)
}

abstract class GenMapper_884_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_884_ : GenMapper_884_<GenModel_884_, String>() {
    override fun map(input: GenModel_884_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_884_ : GenMapper_884_<String, GenModel_884_>() {
    override fun map(input: String): GenModel_884_ {
        val parts = input.split(":")
        return GenModel_884_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_884_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_884_,
    private val saveUseCase: GenSaveUseCase_884_,
    private val deleteUseCase: GenDeleteUseCase_884_,
    private val searchUseCase: GenSearchUseCase_884_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_884_>(GenState_884_.Idle)
    val state: StateFlow<GenState_884_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_884_) {
        when (event) {
            is GenEvent_884_.Load -> loadAll()
            is GenEvent_884_.Update -> save(event.model)
            is GenEvent_884_.Delete -> delete(event.id)
            is GenEvent_884_.Refresh -> loadAll()
            is GenEvent_884_.Search -> search(event.query)
            is GenEvent_884_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_884_.Loading; _state.value = GenState_884_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_884_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_884_.Success(searchUseCase(query)) } }
}
