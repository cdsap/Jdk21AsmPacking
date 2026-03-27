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

data class GenModel_2741_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2741_ {
    data class Load(val id: Long) : GenEvent_2741_()
    data class Update(val model: GenModel_2741_) : GenEvent_2741_()
    data class Delete(val id: Long) : GenEvent_2741_()
    data object Refresh : GenEvent_2741_()
    data class Search(val query: String) : GenEvent_2741_()
    data class Filter(val predicate: String) : GenEvent_2741_()
}

sealed class GenState_2741_ {
    data object Idle : GenState_2741_()
    data object Loading : GenState_2741_()
    data class Success(val items: List<GenModel_2741_>) : GenState_2741_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2741_()
    data class Partial(val items: List<GenModel_2741_>, val hasMore: Boolean) : GenState_2741_()
}

interface GenRepository_2741_ {
    suspend fun getAll(): List<GenModel_2741_>
    suspend fun getById(id: Long): GenModel_2741_?
    suspend fun save(model: GenModel_2741_): GenModel_2741_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2741_>
}

@Singleton
class GenRepositoryImpl_2741_ @Inject constructor() : GenRepository_2741_ {
    private val store = mutableMapOf<Long, GenModel_2741_>()
    override suspend fun getAll(): List<GenModel_2741_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2741_? = store[id]
    override suspend fun save(model: GenModel_2741_): GenModel_2741_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2741_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2741_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2741_ @Inject constructor(
    private val repository: GenRepositoryImpl_2741_
) : GenUseCase_2741_<Unit, List<GenModel_2741_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2741_> = repository.getAll()
}

class GenSaveUseCase_2741_ @Inject constructor(
    private val repository: GenRepositoryImpl_2741_
) : GenUseCase_2741_<GenModel_2741_, GenModel_2741_> {
    override suspend fun invoke(params: GenModel_2741_): GenModel_2741_ = repository.save(params)
}

class GenDeleteUseCase_2741_ @Inject constructor(
    private val repository: GenRepositoryImpl_2741_
) : GenUseCase_2741_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2741_ @Inject constructor(
    private val repository: GenRepositoryImpl_2741_
) : GenUseCase_2741_<String, List<GenModel_2741_>> {
    override suspend fun invoke(params: String): List<GenModel_2741_> = repository.search(params)
}

abstract class GenMapper_2741_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2741_ : GenMapper_2741_<GenModel_2741_, String>() {
    override fun map(input: GenModel_2741_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2741_ : GenMapper_2741_<String, GenModel_2741_>() {
    override fun map(input: String): GenModel_2741_ {
        val parts = input.split(":")
        return GenModel_2741_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2741_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2741_,
    private val saveUseCase: GenSaveUseCase_2741_,
    private val deleteUseCase: GenDeleteUseCase_2741_,
    private val searchUseCase: GenSearchUseCase_2741_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2741_>(GenState_2741_.Idle)
    val state: StateFlow<GenState_2741_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2741_) {
        when (event) {
            is GenEvent_2741_.Load -> loadAll()
            is GenEvent_2741_.Update -> save(event.model)
            is GenEvent_2741_.Delete -> delete(event.id)
            is GenEvent_2741_.Refresh -> loadAll()
            is GenEvent_2741_.Search -> search(event.query)
            is GenEvent_2741_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2741_.Loading; _state.value = GenState_2741_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2741_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2741_.Success(searchUseCase(query)) } }
}
