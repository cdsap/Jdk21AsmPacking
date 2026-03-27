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

data class GenModel_1462_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1462_ {
    data class Load(val id: Long) : GenEvent_1462_()
    data class Update(val model: GenModel_1462_) : GenEvent_1462_()
    data class Delete(val id: Long) : GenEvent_1462_()
    data object Refresh : GenEvent_1462_()
    data class Search(val query: String) : GenEvent_1462_()
    data class Filter(val predicate: String) : GenEvent_1462_()
}

sealed class GenState_1462_ {
    data object Idle : GenState_1462_()
    data object Loading : GenState_1462_()
    data class Success(val items: List<GenModel_1462_>) : GenState_1462_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1462_()
    data class Partial(val items: List<GenModel_1462_>, val hasMore: Boolean) : GenState_1462_()
}

interface GenRepository_1462_ {
    suspend fun getAll(): List<GenModel_1462_>
    suspend fun getById(id: Long): GenModel_1462_?
    suspend fun save(model: GenModel_1462_): GenModel_1462_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1462_>
}

@Singleton
class GenRepositoryImpl_1462_ @Inject constructor() : GenRepository_1462_ {
    private val store = mutableMapOf<Long, GenModel_1462_>()
    override suspend fun getAll(): List<GenModel_1462_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1462_? = store[id]
    override suspend fun save(model: GenModel_1462_): GenModel_1462_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1462_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1462_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1462_ @Inject constructor(
    private val repository: GenRepositoryImpl_1462_
) : GenUseCase_1462_<Unit, List<GenModel_1462_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1462_> = repository.getAll()
}

class GenSaveUseCase_1462_ @Inject constructor(
    private val repository: GenRepositoryImpl_1462_
) : GenUseCase_1462_<GenModel_1462_, GenModel_1462_> {
    override suspend fun invoke(params: GenModel_1462_): GenModel_1462_ = repository.save(params)
}

class GenDeleteUseCase_1462_ @Inject constructor(
    private val repository: GenRepositoryImpl_1462_
) : GenUseCase_1462_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1462_ @Inject constructor(
    private val repository: GenRepositoryImpl_1462_
) : GenUseCase_1462_<String, List<GenModel_1462_>> {
    override suspend fun invoke(params: String): List<GenModel_1462_> = repository.search(params)
}

abstract class GenMapper_1462_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1462_ : GenMapper_1462_<GenModel_1462_, String>() {
    override fun map(input: GenModel_1462_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1462_ : GenMapper_1462_<String, GenModel_1462_>() {
    override fun map(input: String): GenModel_1462_ {
        val parts = input.split(":")
        return GenModel_1462_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1462_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1462_,
    private val saveUseCase: GenSaveUseCase_1462_,
    private val deleteUseCase: GenDeleteUseCase_1462_,
    private val searchUseCase: GenSearchUseCase_1462_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1462_>(GenState_1462_.Idle)
    val state: StateFlow<GenState_1462_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1462_) {
        when (event) {
            is GenEvent_1462_.Load -> loadAll()
            is GenEvent_1462_.Update -> save(event.model)
            is GenEvent_1462_.Delete -> delete(event.id)
            is GenEvent_1462_.Refresh -> loadAll()
            is GenEvent_1462_.Search -> search(event.query)
            is GenEvent_1462_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1462_.Loading; _state.value = GenState_1462_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1462_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1462_.Success(searchUseCase(query)) } }
}
