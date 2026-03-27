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

data class GenModel_824_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_824_ {
    data class Load(val id: Long) : GenEvent_824_()
    data class Update(val model: GenModel_824_) : GenEvent_824_()
    data class Delete(val id: Long) : GenEvent_824_()
    data object Refresh : GenEvent_824_()
    data class Search(val query: String) : GenEvent_824_()
    data class Filter(val predicate: String) : GenEvent_824_()
}

sealed class GenState_824_ {
    data object Idle : GenState_824_()
    data object Loading : GenState_824_()
    data class Success(val items: List<GenModel_824_>) : GenState_824_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_824_()
    data class Partial(val items: List<GenModel_824_>, val hasMore: Boolean) : GenState_824_()
}

interface GenRepository_824_ {
    suspend fun getAll(): List<GenModel_824_>
    suspend fun getById(id: Long): GenModel_824_?
    suspend fun save(model: GenModel_824_): GenModel_824_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_824_>
}

@Singleton
class GenRepositoryImpl_824_ @Inject constructor() : GenRepository_824_ {
    private val store = mutableMapOf<Long, GenModel_824_>()
    override suspend fun getAll(): List<GenModel_824_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_824_? = store[id]
    override suspend fun save(model: GenModel_824_): GenModel_824_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_824_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_824_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_824_ @Inject constructor(
    private val repository: GenRepositoryImpl_824_
) : GenUseCase_824_<Unit, List<GenModel_824_>> {
    override suspend fun invoke(params: Unit): List<GenModel_824_> = repository.getAll()
}

class GenSaveUseCase_824_ @Inject constructor(
    private val repository: GenRepositoryImpl_824_
) : GenUseCase_824_<GenModel_824_, GenModel_824_> {
    override suspend fun invoke(params: GenModel_824_): GenModel_824_ = repository.save(params)
}

class GenDeleteUseCase_824_ @Inject constructor(
    private val repository: GenRepositoryImpl_824_
) : GenUseCase_824_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_824_ @Inject constructor(
    private val repository: GenRepositoryImpl_824_
) : GenUseCase_824_<String, List<GenModel_824_>> {
    override suspend fun invoke(params: String): List<GenModel_824_> = repository.search(params)
}

abstract class GenMapper_824_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_824_ : GenMapper_824_<GenModel_824_, String>() {
    override fun map(input: GenModel_824_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_824_ : GenMapper_824_<String, GenModel_824_>() {
    override fun map(input: String): GenModel_824_ {
        val parts = input.split(":")
        return GenModel_824_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_824_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_824_,
    private val saveUseCase: GenSaveUseCase_824_,
    private val deleteUseCase: GenDeleteUseCase_824_,
    private val searchUseCase: GenSearchUseCase_824_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_824_>(GenState_824_.Idle)
    val state: StateFlow<GenState_824_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_824_) {
        when (event) {
            is GenEvent_824_.Load -> loadAll()
            is GenEvent_824_.Update -> save(event.model)
            is GenEvent_824_.Delete -> delete(event.id)
            is GenEvent_824_.Refresh -> loadAll()
            is GenEvent_824_.Search -> search(event.query)
            is GenEvent_824_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_824_.Loading; _state.value = GenState_824_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_824_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_824_.Success(searchUseCase(query)) } }
}
