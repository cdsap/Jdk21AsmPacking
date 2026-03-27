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

data class GenModel_235_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_235_ {
    data class Load(val id: Long) : GenEvent_235_()
    data class Update(val model: GenModel_235_) : GenEvent_235_()
    data class Delete(val id: Long) : GenEvent_235_()
    data object Refresh : GenEvent_235_()
    data class Search(val query: String) : GenEvent_235_()
    data class Filter(val predicate: String) : GenEvent_235_()
}

sealed class GenState_235_ {
    data object Idle : GenState_235_()
    data object Loading : GenState_235_()
    data class Success(val items: List<GenModel_235_>) : GenState_235_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_235_()
    data class Partial(val items: List<GenModel_235_>, val hasMore: Boolean) : GenState_235_()
}

interface GenRepository_235_ {
    suspend fun getAll(): List<GenModel_235_>
    suspend fun getById(id: Long): GenModel_235_?
    suspend fun save(model: GenModel_235_): GenModel_235_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_235_>
}

@Singleton
class GenRepositoryImpl_235_ @Inject constructor() : GenRepository_235_ {
    private val store = mutableMapOf<Long, GenModel_235_>()
    override suspend fun getAll(): List<GenModel_235_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_235_? = store[id]
    override suspend fun save(model: GenModel_235_): GenModel_235_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_235_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_235_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_235_ @Inject constructor(
    private val repository: GenRepositoryImpl_235_
) : GenUseCase_235_<Unit, List<GenModel_235_>> {
    override suspend fun invoke(params: Unit): List<GenModel_235_> = repository.getAll()
}

class GenSaveUseCase_235_ @Inject constructor(
    private val repository: GenRepositoryImpl_235_
) : GenUseCase_235_<GenModel_235_, GenModel_235_> {
    override suspend fun invoke(params: GenModel_235_): GenModel_235_ = repository.save(params)
}

class GenDeleteUseCase_235_ @Inject constructor(
    private val repository: GenRepositoryImpl_235_
) : GenUseCase_235_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_235_ @Inject constructor(
    private val repository: GenRepositoryImpl_235_
) : GenUseCase_235_<String, List<GenModel_235_>> {
    override suspend fun invoke(params: String): List<GenModel_235_> = repository.search(params)
}

abstract class GenMapper_235_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_235_ : GenMapper_235_<GenModel_235_, String>() {
    override fun map(input: GenModel_235_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_235_ : GenMapper_235_<String, GenModel_235_>() {
    override fun map(input: String): GenModel_235_ {
        val parts = input.split(":")
        return GenModel_235_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_235_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_235_,
    private val saveUseCase: GenSaveUseCase_235_,
    private val deleteUseCase: GenDeleteUseCase_235_,
    private val searchUseCase: GenSearchUseCase_235_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_235_>(GenState_235_.Idle)
    val state: StateFlow<GenState_235_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_235_) {
        when (event) {
            is GenEvent_235_.Load -> loadAll()
            is GenEvent_235_.Update -> save(event.model)
            is GenEvent_235_.Delete -> delete(event.id)
            is GenEvent_235_.Refresh -> loadAll()
            is GenEvent_235_.Search -> search(event.query)
            is GenEvent_235_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_235_.Loading; _state.value = GenState_235_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_235_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_235_.Success(searchUseCase(query)) } }
}
