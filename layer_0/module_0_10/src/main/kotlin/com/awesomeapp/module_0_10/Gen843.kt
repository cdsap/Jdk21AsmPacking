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

data class GenModel_843_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_843_ {
    data class Load(val id: Long) : GenEvent_843_()
    data class Update(val model: GenModel_843_) : GenEvent_843_()
    data class Delete(val id: Long) : GenEvent_843_()
    data object Refresh : GenEvent_843_()
    data class Search(val query: String) : GenEvent_843_()
    data class Filter(val predicate: String) : GenEvent_843_()
}

sealed class GenState_843_ {
    data object Idle : GenState_843_()
    data object Loading : GenState_843_()
    data class Success(val items: List<GenModel_843_>) : GenState_843_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_843_()
    data class Partial(val items: List<GenModel_843_>, val hasMore: Boolean) : GenState_843_()
}

interface GenRepository_843_ {
    suspend fun getAll(): List<GenModel_843_>
    suspend fun getById(id: Long): GenModel_843_?
    suspend fun save(model: GenModel_843_): GenModel_843_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_843_>
}

@Singleton
class GenRepositoryImpl_843_ @Inject constructor() : GenRepository_843_ {
    private val store = mutableMapOf<Long, GenModel_843_>()
    override suspend fun getAll(): List<GenModel_843_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_843_? = store[id]
    override suspend fun save(model: GenModel_843_): GenModel_843_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_843_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_843_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_843_ @Inject constructor(
    private val repository: GenRepositoryImpl_843_
) : GenUseCase_843_<Unit, List<GenModel_843_>> {
    override suspend fun invoke(params: Unit): List<GenModel_843_> = repository.getAll()
}

class GenSaveUseCase_843_ @Inject constructor(
    private val repository: GenRepositoryImpl_843_
) : GenUseCase_843_<GenModel_843_, GenModel_843_> {
    override suspend fun invoke(params: GenModel_843_): GenModel_843_ = repository.save(params)
}

class GenDeleteUseCase_843_ @Inject constructor(
    private val repository: GenRepositoryImpl_843_
) : GenUseCase_843_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_843_ @Inject constructor(
    private val repository: GenRepositoryImpl_843_
) : GenUseCase_843_<String, List<GenModel_843_>> {
    override suspend fun invoke(params: String): List<GenModel_843_> = repository.search(params)
}

abstract class GenMapper_843_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_843_ : GenMapper_843_<GenModel_843_, String>() {
    override fun map(input: GenModel_843_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_843_ : GenMapper_843_<String, GenModel_843_>() {
    override fun map(input: String): GenModel_843_ {
        val parts = input.split(":")
        return GenModel_843_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_843_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_843_,
    private val saveUseCase: GenSaveUseCase_843_,
    private val deleteUseCase: GenDeleteUseCase_843_,
    private val searchUseCase: GenSearchUseCase_843_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_843_>(GenState_843_.Idle)
    val state: StateFlow<GenState_843_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_843_) {
        when (event) {
            is GenEvent_843_.Load -> loadAll()
            is GenEvent_843_.Update -> save(event.model)
            is GenEvent_843_.Delete -> delete(event.id)
            is GenEvent_843_.Refresh -> loadAll()
            is GenEvent_843_.Search -> search(event.query)
            is GenEvent_843_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_843_.Loading; _state.value = GenState_843_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_843_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_843_.Success(searchUseCase(query)) } }
}
