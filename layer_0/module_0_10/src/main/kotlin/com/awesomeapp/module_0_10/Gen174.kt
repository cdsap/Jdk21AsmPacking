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

data class GenModel_174_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_174_ {
    data class Load(val id: Long) : GenEvent_174_()
    data class Update(val model: GenModel_174_) : GenEvent_174_()
    data class Delete(val id: Long) : GenEvent_174_()
    data object Refresh : GenEvent_174_()
    data class Search(val query: String) : GenEvent_174_()
    data class Filter(val predicate: String) : GenEvent_174_()
}

sealed class GenState_174_ {
    data object Idle : GenState_174_()
    data object Loading : GenState_174_()
    data class Success(val items: List<GenModel_174_>) : GenState_174_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_174_()
    data class Partial(val items: List<GenModel_174_>, val hasMore: Boolean) : GenState_174_()
}

interface GenRepository_174_ {
    suspend fun getAll(): List<GenModel_174_>
    suspend fun getById(id: Long): GenModel_174_?
    suspend fun save(model: GenModel_174_): GenModel_174_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_174_>
}

@Singleton
class GenRepositoryImpl_174_ @Inject constructor() : GenRepository_174_ {
    private val store = mutableMapOf<Long, GenModel_174_>()
    override suspend fun getAll(): List<GenModel_174_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_174_? = store[id]
    override suspend fun save(model: GenModel_174_): GenModel_174_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_174_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_174_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_174_ @Inject constructor(
    private val repository: GenRepositoryImpl_174_
) : GenUseCase_174_<Unit, List<GenModel_174_>> {
    override suspend fun invoke(params: Unit): List<GenModel_174_> = repository.getAll()
}

class GenSaveUseCase_174_ @Inject constructor(
    private val repository: GenRepositoryImpl_174_
) : GenUseCase_174_<GenModel_174_, GenModel_174_> {
    override suspend fun invoke(params: GenModel_174_): GenModel_174_ = repository.save(params)
}

class GenDeleteUseCase_174_ @Inject constructor(
    private val repository: GenRepositoryImpl_174_
) : GenUseCase_174_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_174_ @Inject constructor(
    private val repository: GenRepositoryImpl_174_
) : GenUseCase_174_<String, List<GenModel_174_>> {
    override suspend fun invoke(params: String): List<GenModel_174_> = repository.search(params)
}

abstract class GenMapper_174_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_174_ : GenMapper_174_<GenModel_174_, String>() {
    override fun map(input: GenModel_174_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_174_ : GenMapper_174_<String, GenModel_174_>() {
    override fun map(input: String): GenModel_174_ {
        val parts = input.split(":")
        return GenModel_174_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_174_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_174_,
    private val saveUseCase: GenSaveUseCase_174_,
    private val deleteUseCase: GenDeleteUseCase_174_,
    private val searchUseCase: GenSearchUseCase_174_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_174_>(GenState_174_.Idle)
    val state: StateFlow<GenState_174_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_174_) {
        when (event) {
            is GenEvent_174_.Load -> loadAll()
            is GenEvent_174_.Update -> save(event.model)
            is GenEvent_174_.Delete -> delete(event.id)
            is GenEvent_174_.Refresh -> loadAll()
            is GenEvent_174_.Search -> search(event.query)
            is GenEvent_174_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_174_.Loading; _state.value = GenState_174_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_174_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_174_.Success(searchUseCase(query)) } }
}
