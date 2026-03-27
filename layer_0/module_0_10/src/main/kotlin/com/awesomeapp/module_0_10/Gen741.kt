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

data class GenModel_741_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_741_ {
    data class Load(val id: Long) : GenEvent_741_()
    data class Update(val model: GenModel_741_) : GenEvent_741_()
    data class Delete(val id: Long) : GenEvent_741_()
    data object Refresh : GenEvent_741_()
    data class Search(val query: String) : GenEvent_741_()
    data class Filter(val predicate: String) : GenEvent_741_()
}

sealed class GenState_741_ {
    data object Idle : GenState_741_()
    data object Loading : GenState_741_()
    data class Success(val items: List<GenModel_741_>) : GenState_741_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_741_()
    data class Partial(val items: List<GenModel_741_>, val hasMore: Boolean) : GenState_741_()
}

interface GenRepository_741_ {
    suspend fun getAll(): List<GenModel_741_>
    suspend fun getById(id: Long): GenModel_741_?
    suspend fun save(model: GenModel_741_): GenModel_741_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_741_>
}

@Singleton
class GenRepositoryImpl_741_ @Inject constructor() : GenRepository_741_ {
    private val store = mutableMapOf<Long, GenModel_741_>()
    override suspend fun getAll(): List<GenModel_741_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_741_? = store[id]
    override suspend fun save(model: GenModel_741_): GenModel_741_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_741_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_741_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_741_ @Inject constructor(
    private val repository: GenRepositoryImpl_741_
) : GenUseCase_741_<Unit, List<GenModel_741_>> {
    override suspend fun invoke(params: Unit): List<GenModel_741_> = repository.getAll()
}

class GenSaveUseCase_741_ @Inject constructor(
    private val repository: GenRepositoryImpl_741_
) : GenUseCase_741_<GenModel_741_, GenModel_741_> {
    override suspend fun invoke(params: GenModel_741_): GenModel_741_ = repository.save(params)
}

class GenDeleteUseCase_741_ @Inject constructor(
    private val repository: GenRepositoryImpl_741_
) : GenUseCase_741_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_741_ @Inject constructor(
    private val repository: GenRepositoryImpl_741_
) : GenUseCase_741_<String, List<GenModel_741_>> {
    override suspend fun invoke(params: String): List<GenModel_741_> = repository.search(params)
}

abstract class GenMapper_741_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_741_ : GenMapper_741_<GenModel_741_, String>() {
    override fun map(input: GenModel_741_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_741_ : GenMapper_741_<String, GenModel_741_>() {
    override fun map(input: String): GenModel_741_ {
        val parts = input.split(":")
        return GenModel_741_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_741_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_741_,
    private val saveUseCase: GenSaveUseCase_741_,
    private val deleteUseCase: GenDeleteUseCase_741_,
    private val searchUseCase: GenSearchUseCase_741_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_741_>(GenState_741_.Idle)
    val state: StateFlow<GenState_741_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_741_) {
        when (event) {
            is GenEvent_741_.Load -> loadAll()
            is GenEvent_741_.Update -> save(event.model)
            is GenEvent_741_.Delete -> delete(event.id)
            is GenEvent_741_.Refresh -> loadAll()
            is GenEvent_741_.Search -> search(event.query)
            is GenEvent_741_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_741_.Loading; _state.value = GenState_741_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_741_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_741_.Success(searchUseCase(query)) } }
}
