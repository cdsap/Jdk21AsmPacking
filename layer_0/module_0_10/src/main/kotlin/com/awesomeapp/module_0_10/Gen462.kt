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

data class GenModel_462_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_462_ {
    data class Load(val id: Long) : GenEvent_462_()
    data class Update(val model: GenModel_462_) : GenEvent_462_()
    data class Delete(val id: Long) : GenEvent_462_()
    data object Refresh : GenEvent_462_()
    data class Search(val query: String) : GenEvent_462_()
    data class Filter(val predicate: String) : GenEvent_462_()
}

sealed class GenState_462_ {
    data object Idle : GenState_462_()
    data object Loading : GenState_462_()
    data class Success(val items: List<GenModel_462_>) : GenState_462_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_462_()
    data class Partial(val items: List<GenModel_462_>, val hasMore: Boolean) : GenState_462_()
}

interface GenRepository_462_ {
    suspend fun getAll(): List<GenModel_462_>
    suspend fun getById(id: Long): GenModel_462_?
    suspend fun save(model: GenModel_462_): GenModel_462_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_462_>
}

@Singleton
class GenRepositoryImpl_462_ @Inject constructor() : GenRepository_462_ {
    private val store = mutableMapOf<Long, GenModel_462_>()
    override suspend fun getAll(): List<GenModel_462_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_462_? = store[id]
    override suspend fun save(model: GenModel_462_): GenModel_462_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_462_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_462_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_462_ @Inject constructor(
    private val repository: GenRepositoryImpl_462_
) : GenUseCase_462_<Unit, List<GenModel_462_>> {
    override suspend fun invoke(params: Unit): List<GenModel_462_> = repository.getAll()
}

class GenSaveUseCase_462_ @Inject constructor(
    private val repository: GenRepositoryImpl_462_
) : GenUseCase_462_<GenModel_462_, GenModel_462_> {
    override suspend fun invoke(params: GenModel_462_): GenModel_462_ = repository.save(params)
}

class GenDeleteUseCase_462_ @Inject constructor(
    private val repository: GenRepositoryImpl_462_
) : GenUseCase_462_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_462_ @Inject constructor(
    private val repository: GenRepositoryImpl_462_
) : GenUseCase_462_<String, List<GenModel_462_>> {
    override suspend fun invoke(params: String): List<GenModel_462_> = repository.search(params)
}

abstract class GenMapper_462_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_462_ : GenMapper_462_<GenModel_462_, String>() {
    override fun map(input: GenModel_462_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_462_ : GenMapper_462_<String, GenModel_462_>() {
    override fun map(input: String): GenModel_462_ {
        val parts = input.split(":")
        return GenModel_462_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_462_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_462_,
    private val saveUseCase: GenSaveUseCase_462_,
    private val deleteUseCase: GenDeleteUseCase_462_,
    private val searchUseCase: GenSearchUseCase_462_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_462_>(GenState_462_.Idle)
    val state: StateFlow<GenState_462_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_462_) {
        when (event) {
            is GenEvent_462_.Load -> loadAll()
            is GenEvent_462_.Update -> save(event.model)
            is GenEvent_462_.Delete -> delete(event.id)
            is GenEvent_462_.Refresh -> loadAll()
            is GenEvent_462_.Search -> search(event.query)
            is GenEvent_462_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_462_.Loading; _state.value = GenState_462_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_462_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_462_.Success(searchUseCase(query)) } }
}
