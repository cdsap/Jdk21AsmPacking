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

data class GenModel_3345_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3345_ {
    data class Load(val id: Long) : GenEvent_3345_()
    data class Update(val model: GenModel_3345_) : GenEvent_3345_()
    data class Delete(val id: Long) : GenEvent_3345_()
    data object Refresh : GenEvent_3345_()
    data class Search(val query: String) : GenEvent_3345_()
    data class Filter(val predicate: String) : GenEvent_3345_()
}

sealed class GenState_3345_ {
    data object Idle : GenState_3345_()
    data object Loading : GenState_3345_()
    data class Success(val items: List<GenModel_3345_>) : GenState_3345_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3345_()
    data class Partial(val items: List<GenModel_3345_>, val hasMore: Boolean) : GenState_3345_()
}

interface GenRepository_3345_ {
    suspend fun getAll(): List<GenModel_3345_>
    suspend fun getById(id: Long): GenModel_3345_?
    suspend fun save(model: GenModel_3345_): GenModel_3345_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3345_>
}

@Singleton
class GenRepositoryImpl_3345_ @Inject constructor() : GenRepository_3345_ {
    private val store = mutableMapOf<Long, GenModel_3345_>()
    override suspend fun getAll(): List<GenModel_3345_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3345_? = store[id]
    override suspend fun save(model: GenModel_3345_): GenModel_3345_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3345_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3345_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3345_ @Inject constructor(
    private val repository: GenRepositoryImpl_3345_
) : GenUseCase_3345_<Unit, List<GenModel_3345_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3345_> = repository.getAll()
}

class GenSaveUseCase_3345_ @Inject constructor(
    private val repository: GenRepositoryImpl_3345_
) : GenUseCase_3345_<GenModel_3345_, GenModel_3345_> {
    override suspend fun invoke(params: GenModel_3345_): GenModel_3345_ = repository.save(params)
}

class GenDeleteUseCase_3345_ @Inject constructor(
    private val repository: GenRepositoryImpl_3345_
) : GenUseCase_3345_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3345_ @Inject constructor(
    private val repository: GenRepositoryImpl_3345_
) : GenUseCase_3345_<String, List<GenModel_3345_>> {
    override suspend fun invoke(params: String): List<GenModel_3345_> = repository.search(params)
}

abstract class GenMapper_3345_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3345_ : GenMapper_3345_<GenModel_3345_, String>() {
    override fun map(input: GenModel_3345_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3345_ : GenMapper_3345_<String, GenModel_3345_>() {
    override fun map(input: String): GenModel_3345_ {
        val parts = input.split(":")
        return GenModel_3345_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3345_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3345_,
    private val saveUseCase: GenSaveUseCase_3345_,
    private val deleteUseCase: GenDeleteUseCase_3345_,
    private val searchUseCase: GenSearchUseCase_3345_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3345_>(GenState_3345_.Idle)
    val state: StateFlow<GenState_3345_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3345_) {
        when (event) {
            is GenEvent_3345_.Load -> loadAll()
            is GenEvent_3345_.Update -> save(event.model)
            is GenEvent_3345_.Delete -> delete(event.id)
            is GenEvent_3345_.Refresh -> loadAll()
            is GenEvent_3345_.Search -> search(event.query)
            is GenEvent_3345_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3345_.Loading; _state.value = GenState_3345_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3345_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3345_.Success(searchUseCase(query)) } }
}
