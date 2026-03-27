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

data class GenModel_1165_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1165_ {
    data class Load(val id: Long) : GenEvent_1165_()
    data class Update(val model: GenModel_1165_) : GenEvent_1165_()
    data class Delete(val id: Long) : GenEvent_1165_()
    data object Refresh : GenEvent_1165_()
    data class Search(val query: String) : GenEvent_1165_()
    data class Filter(val predicate: String) : GenEvent_1165_()
}

sealed class GenState_1165_ {
    data object Idle : GenState_1165_()
    data object Loading : GenState_1165_()
    data class Success(val items: List<GenModel_1165_>) : GenState_1165_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1165_()
    data class Partial(val items: List<GenModel_1165_>, val hasMore: Boolean) : GenState_1165_()
}

interface GenRepository_1165_ {
    suspend fun getAll(): List<GenModel_1165_>
    suspend fun getById(id: Long): GenModel_1165_?
    suspend fun save(model: GenModel_1165_): GenModel_1165_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1165_>
}

@Singleton
class GenRepositoryImpl_1165_ @Inject constructor() : GenRepository_1165_ {
    private val store = mutableMapOf<Long, GenModel_1165_>()
    override suspend fun getAll(): List<GenModel_1165_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1165_? = store[id]
    override suspend fun save(model: GenModel_1165_): GenModel_1165_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1165_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1165_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1165_ @Inject constructor(
    private val repository: GenRepositoryImpl_1165_
) : GenUseCase_1165_<Unit, List<GenModel_1165_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1165_> = repository.getAll()
}

class GenSaveUseCase_1165_ @Inject constructor(
    private val repository: GenRepositoryImpl_1165_
) : GenUseCase_1165_<GenModel_1165_, GenModel_1165_> {
    override suspend fun invoke(params: GenModel_1165_): GenModel_1165_ = repository.save(params)
}

class GenDeleteUseCase_1165_ @Inject constructor(
    private val repository: GenRepositoryImpl_1165_
) : GenUseCase_1165_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1165_ @Inject constructor(
    private val repository: GenRepositoryImpl_1165_
) : GenUseCase_1165_<String, List<GenModel_1165_>> {
    override suspend fun invoke(params: String): List<GenModel_1165_> = repository.search(params)
}

abstract class GenMapper_1165_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1165_ : GenMapper_1165_<GenModel_1165_, String>() {
    override fun map(input: GenModel_1165_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1165_ : GenMapper_1165_<String, GenModel_1165_>() {
    override fun map(input: String): GenModel_1165_ {
        val parts = input.split(":")
        return GenModel_1165_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1165_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1165_,
    private val saveUseCase: GenSaveUseCase_1165_,
    private val deleteUseCase: GenDeleteUseCase_1165_,
    private val searchUseCase: GenSearchUseCase_1165_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1165_>(GenState_1165_.Idle)
    val state: StateFlow<GenState_1165_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1165_) {
        when (event) {
            is GenEvent_1165_.Load -> loadAll()
            is GenEvent_1165_.Update -> save(event.model)
            is GenEvent_1165_.Delete -> delete(event.id)
            is GenEvent_1165_.Refresh -> loadAll()
            is GenEvent_1165_.Search -> search(event.query)
            is GenEvent_1165_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1165_.Loading; _state.value = GenState_1165_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1165_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1165_.Success(searchUseCase(query)) } }
}
