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

data class GenModel_102_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_102_ {
    data class Load(val id: Long) : GenEvent_102_()
    data class Update(val model: GenModel_102_) : GenEvent_102_()
    data class Delete(val id: Long) : GenEvent_102_()
    data object Refresh : GenEvent_102_()
    data class Search(val query: String) : GenEvent_102_()
    data class Filter(val predicate: String) : GenEvent_102_()
}

sealed class GenState_102_ {
    data object Idle : GenState_102_()
    data object Loading : GenState_102_()
    data class Success(val items: List<GenModel_102_>) : GenState_102_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_102_()
    data class Partial(val items: List<GenModel_102_>, val hasMore: Boolean) : GenState_102_()
}

interface GenRepository_102_ {
    suspend fun getAll(): List<GenModel_102_>
    suspend fun getById(id: Long): GenModel_102_?
    suspend fun save(model: GenModel_102_): GenModel_102_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_102_>
}

@Singleton
class GenRepositoryImpl_102_ @Inject constructor() : GenRepository_102_ {
    private val store = mutableMapOf<Long, GenModel_102_>()
    override suspend fun getAll(): List<GenModel_102_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_102_? = store[id]
    override suspend fun save(model: GenModel_102_): GenModel_102_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_102_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_102_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_102_ @Inject constructor(
    private val repository: GenRepositoryImpl_102_
) : GenUseCase_102_<Unit, List<GenModel_102_>> {
    override suspend fun invoke(params: Unit): List<GenModel_102_> = repository.getAll()
}

class GenSaveUseCase_102_ @Inject constructor(
    private val repository: GenRepositoryImpl_102_
) : GenUseCase_102_<GenModel_102_, GenModel_102_> {
    override suspend fun invoke(params: GenModel_102_): GenModel_102_ = repository.save(params)
}

class GenDeleteUseCase_102_ @Inject constructor(
    private val repository: GenRepositoryImpl_102_
) : GenUseCase_102_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_102_ @Inject constructor(
    private val repository: GenRepositoryImpl_102_
) : GenUseCase_102_<String, List<GenModel_102_>> {
    override suspend fun invoke(params: String): List<GenModel_102_> = repository.search(params)
}

abstract class GenMapper_102_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_102_ : GenMapper_102_<GenModel_102_, String>() {
    override fun map(input: GenModel_102_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_102_ : GenMapper_102_<String, GenModel_102_>() {
    override fun map(input: String): GenModel_102_ {
        val parts = input.split(":")
        return GenModel_102_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_102_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_102_,
    private val saveUseCase: GenSaveUseCase_102_,
    private val deleteUseCase: GenDeleteUseCase_102_,
    private val searchUseCase: GenSearchUseCase_102_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_102_>(GenState_102_.Idle)
    val state: StateFlow<GenState_102_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_102_) {
        when (event) {
            is GenEvent_102_.Load -> loadAll()
            is GenEvent_102_.Update -> save(event.model)
            is GenEvent_102_.Delete -> delete(event.id)
            is GenEvent_102_.Refresh -> loadAll()
            is GenEvent_102_.Search -> search(event.query)
            is GenEvent_102_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_102_.Loading; _state.value = GenState_102_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_102_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_102_.Success(searchUseCase(query)) } }
}
