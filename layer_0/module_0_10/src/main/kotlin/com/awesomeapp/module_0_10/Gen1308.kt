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

data class GenModel_1308_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1308_ {
    data class Load(val id: Long) : GenEvent_1308_()
    data class Update(val model: GenModel_1308_) : GenEvent_1308_()
    data class Delete(val id: Long) : GenEvent_1308_()
    data object Refresh : GenEvent_1308_()
    data class Search(val query: String) : GenEvent_1308_()
    data class Filter(val predicate: String) : GenEvent_1308_()
}

sealed class GenState_1308_ {
    data object Idle : GenState_1308_()
    data object Loading : GenState_1308_()
    data class Success(val items: List<GenModel_1308_>) : GenState_1308_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1308_()
    data class Partial(val items: List<GenModel_1308_>, val hasMore: Boolean) : GenState_1308_()
}

interface GenRepository_1308_ {
    suspend fun getAll(): List<GenModel_1308_>
    suspend fun getById(id: Long): GenModel_1308_?
    suspend fun save(model: GenModel_1308_): GenModel_1308_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1308_>
}

@Singleton
class GenRepositoryImpl_1308_ @Inject constructor() : GenRepository_1308_ {
    private val store = mutableMapOf<Long, GenModel_1308_>()
    override suspend fun getAll(): List<GenModel_1308_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1308_? = store[id]
    override suspend fun save(model: GenModel_1308_): GenModel_1308_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1308_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1308_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1308_ @Inject constructor(
    private val repository: GenRepositoryImpl_1308_
) : GenUseCase_1308_<Unit, List<GenModel_1308_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1308_> = repository.getAll()
}

class GenSaveUseCase_1308_ @Inject constructor(
    private val repository: GenRepositoryImpl_1308_
) : GenUseCase_1308_<GenModel_1308_, GenModel_1308_> {
    override suspend fun invoke(params: GenModel_1308_): GenModel_1308_ = repository.save(params)
}

class GenDeleteUseCase_1308_ @Inject constructor(
    private val repository: GenRepositoryImpl_1308_
) : GenUseCase_1308_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1308_ @Inject constructor(
    private val repository: GenRepositoryImpl_1308_
) : GenUseCase_1308_<String, List<GenModel_1308_>> {
    override suspend fun invoke(params: String): List<GenModel_1308_> = repository.search(params)
}

abstract class GenMapper_1308_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1308_ : GenMapper_1308_<GenModel_1308_, String>() {
    override fun map(input: GenModel_1308_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1308_ : GenMapper_1308_<String, GenModel_1308_>() {
    override fun map(input: String): GenModel_1308_ {
        val parts = input.split(":")
        return GenModel_1308_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1308_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1308_,
    private val saveUseCase: GenSaveUseCase_1308_,
    private val deleteUseCase: GenDeleteUseCase_1308_,
    private val searchUseCase: GenSearchUseCase_1308_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1308_>(GenState_1308_.Idle)
    val state: StateFlow<GenState_1308_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1308_) {
        when (event) {
            is GenEvent_1308_.Load -> loadAll()
            is GenEvent_1308_.Update -> save(event.model)
            is GenEvent_1308_.Delete -> delete(event.id)
            is GenEvent_1308_.Refresh -> loadAll()
            is GenEvent_1308_.Search -> search(event.query)
            is GenEvent_1308_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1308_.Loading; _state.value = GenState_1308_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1308_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1308_.Success(searchUseCase(query)) } }
}
