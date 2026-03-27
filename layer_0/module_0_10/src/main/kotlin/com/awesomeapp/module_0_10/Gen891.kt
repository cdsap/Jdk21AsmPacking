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

data class GenModel_891_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_891_ {
    data class Load(val id: Long) : GenEvent_891_()
    data class Update(val model: GenModel_891_) : GenEvent_891_()
    data class Delete(val id: Long) : GenEvent_891_()
    data object Refresh : GenEvent_891_()
    data class Search(val query: String) : GenEvent_891_()
    data class Filter(val predicate: String) : GenEvent_891_()
}

sealed class GenState_891_ {
    data object Idle : GenState_891_()
    data object Loading : GenState_891_()
    data class Success(val items: List<GenModel_891_>) : GenState_891_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_891_()
    data class Partial(val items: List<GenModel_891_>, val hasMore: Boolean) : GenState_891_()
}

interface GenRepository_891_ {
    suspend fun getAll(): List<GenModel_891_>
    suspend fun getById(id: Long): GenModel_891_?
    suspend fun save(model: GenModel_891_): GenModel_891_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_891_>
}

@Singleton
class GenRepositoryImpl_891_ @Inject constructor() : GenRepository_891_ {
    private val store = mutableMapOf<Long, GenModel_891_>()
    override suspend fun getAll(): List<GenModel_891_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_891_? = store[id]
    override suspend fun save(model: GenModel_891_): GenModel_891_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_891_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_891_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_891_ @Inject constructor(
    private val repository: GenRepositoryImpl_891_
) : GenUseCase_891_<Unit, List<GenModel_891_>> {
    override suspend fun invoke(params: Unit): List<GenModel_891_> = repository.getAll()
}

class GenSaveUseCase_891_ @Inject constructor(
    private val repository: GenRepositoryImpl_891_
) : GenUseCase_891_<GenModel_891_, GenModel_891_> {
    override suspend fun invoke(params: GenModel_891_): GenModel_891_ = repository.save(params)
}

class GenDeleteUseCase_891_ @Inject constructor(
    private val repository: GenRepositoryImpl_891_
) : GenUseCase_891_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_891_ @Inject constructor(
    private val repository: GenRepositoryImpl_891_
) : GenUseCase_891_<String, List<GenModel_891_>> {
    override suspend fun invoke(params: String): List<GenModel_891_> = repository.search(params)
}

abstract class GenMapper_891_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_891_ : GenMapper_891_<GenModel_891_, String>() {
    override fun map(input: GenModel_891_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_891_ : GenMapper_891_<String, GenModel_891_>() {
    override fun map(input: String): GenModel_891_ {
        val parts = input.split(":")
        return GenModel_891_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_891_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_891_,
    private val saveUseCase: GenSaveUseCase_891_,
    private val deleteUseCase: GenDeleteUseCase_891_,
    private val searchUseCase: GenSearchUseCase_891_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_891_>(GenState_891_.Idle)
    val state: StateFlow<GenState_891_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_891_) {
        when (event) {
            is GenEvent_891_.Load -> loadAll()
            is GenEvent_891_.Update -> save(event.model)
            is GenEvent_891_.Delete -> delete(event.id)
            is GenEvent_891_.Refresh -> loadAll()
            is GenEvent_891_.Search -> search(event.query)
            is GenEvent_891_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_891_.Loading; _state.value = GenState_891_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_891_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_891_.Success(searchUseCase(query)) } }
}
