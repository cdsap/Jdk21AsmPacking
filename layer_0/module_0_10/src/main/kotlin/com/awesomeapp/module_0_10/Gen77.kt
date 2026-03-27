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

data class GenModel_77_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_77_ {
    data class Load(val id: Long) : GenEvent_77_()
    data class Update(val model: GenModel_77_) : GenEvent_77_()
    data class Delete(val id: Long) : GenEvent_77_()
    data object Refresh : GenEvent_77_()
    data class Search(val query: String) : GenEvent_77_()
    data class Filter(val predicate: String) : GenEvent_77_()
}

sealed class GenState_77_ {
    data object Idle : GenState_77_()
    data object Loading : GenState_77_()
    data class Success(val items: List<GenModel_77_>) : GenState_77_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_77_()
    data class Partial(val items: List<GenModel_77_>, val hasMore: Boolean) : GenState_77_()
}

interface GenRepository_77_ {
    suspend fun getAll(): List<GenModel_77_>
    suspend fun getById(id: Long): GenModel_77_?
    suspend fun save(model: GenModel_77_): GenModel_77_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_77_>
}

@Singleton
class GenRepositoryImpl_77_ @Inject constructor() : GenRepository_77_ {
    private val store = mutableMapOf<Long, GenModel_77_>()
    override suspend fun getAll(): List<GenModel_77_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_77_? = store[id]
    override suspend fun save(model: GenModel_77_): GenModel_77_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_77_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_77_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_77_ @Inject constructor(
    private val repository: GenRepositoryImpl_77_
) : GenUseCase_77_<Unit, List<GenModel_77_>> {
    override suspend fun invoke(params: Unit): List<GenModel_77_> = repository.getAll()
}

class GenSaveUseCase_77_ @Inject constructor(
    private val repository: GenRepositoryImpl_77_
) : GenUseCase_77_<GenModel_77_, GenModel_77_> {
    override suspend fun invoke(params: GenModel_77_): GenModel_77_ = repository.save(params)
}

class GenDeleteUseCase_77_ @Inject constructor(
    private val repository: GenRepositoryImpl_77_
) : GenUseCase_77_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_77_ @Inject constructor(
    private val repository: GenRepositoryImpl_77_
) : GenUseCase_77_<String, List<GenModel_77_>> {
    override suspend fun invoke(params: String): List<GenModel_77_> = repository.search(params)
}

abstract class GenMapper_77_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_77_ : GenMapper_77_<GenModel_77_, String>() {
    override fun map(input: GenModel_77_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_77_ : GenMapper_77_<String, GenModel_77_>() {
    override fun map(input: String): GenModel_77_ {
        val parts = input.split(":")
        return GenModel_77_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_77_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_77_,
    private val saveUseCase: GenSaveUseCase_77_,
    private val deleteUseCase: GenDeleteUseCase_77_,
    private val searchUseCase: GenSearchUseCase_77_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_77_>(GenState_77_.Idle)
    val state: StateFlow<GenState_77_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_77_) {
        when (event) {
            is GenEvent_77_.Load -> loadAll()
            is GenEvent_77_.Update -> save(event.model)
            is GenEvent_77_.Delete -> delete(event.id)
            is GenEvent_77_.Refresh -> loadAll()
            is GenEvent_77_.Search -> search(event.query)
            is GenEvent_77_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_77_.Loading; _state.value = GenState_77_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_77_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_77_.Success(searchUseCase(query)) } }
}
