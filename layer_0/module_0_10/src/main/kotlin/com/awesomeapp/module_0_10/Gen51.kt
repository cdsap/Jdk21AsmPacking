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

data class GenModel_51_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_51_ {
    data class Load(val id: Long) : GenEvent_51_()
    data class Update(val model: GenModel_51_) : GenEvent_51_()
    data class Delete(val id: Long) : GenEvent_51_()
    data object Refresh : GenEvent_51_()
    data class Search(val query: String) : GenEvent_51_()
    data class Filter(val predicate: String) : GenEvent_51_()
}

sealed class GenState_51_ {
    data object Idle : GenState_51_()
    data object Loading : GenState_51_()
    data class Success(val items: List<GenModel_51_>) : GenState_51_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_51_()
    data class Partial(val items: List<GenModel_51_>, val hasMore: Boolean) : GenState_51_()
}

interface GenRepository_51_ {
    suspend fun getAll(): List<GenModel_51_>
    suspend fun getById(id: Long): GenModel_51_?
    suspend fun save(model: GenModel_51_): GenModel_51_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_51_>
}

@Singleton
class GenRepositoryImpl_51_ @Inject constructor() : GenRepository_51_ {
    private val store = mutableMapOf<Long, GenModel_51_>()
    override suspend fun getAll(): List<GenModel_51_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_51_? = store[id]
    override suspend fun save(model: GenModel_51_): GenModel_51_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_51_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_51_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_51_ @Inject constructor(
    private val repository: GenRepositoryImpl_51_
) : GenUseCase_51_<Unit, List<GenModel_51_>> {
    override suspend fun invoke(params: Unit): List<GenModel_51_> = repository.getAll()
}

class GenSaveUseCase_51_ @Inject constructor(
    private val repository: GenRepositoryImpl_51_
) : GenUseCase_51_<GenModel_51_, GenModel_51_> {
    override suspend fun invoke(params: GenModel_51_): GenModel_51_ = repository.save(params)
}

class GenDeleteUseCase_51_ @Inject constructor(
    private val repository: GenRepositoryImpl_51_
) : GenUseCase_51_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_51_ @Inject constructor(
    private val repository: GenRepositoryImpl_51_
) : GenUseCase_51_<String, List<GenModel_51_>> {
    override suspend fun invoke(params: String): List<GenModel_51_> = repository.search(params)
}

abstract class GenMapper_51_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_51_ : GenMapper_51_<GenModel_51_, String>() {
    override fun map(input: GenModel_51_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_51_ : GenMapper_51_<String, GenModel_51_>() {
    override fun map(input: String): GenModel_51_ {
        val parts = input.split(":")
        return GenModel_51_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_51_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_51_,
    private val saveUseCase: GenSaveUseCase_51_,
    private val deleteUseCase: GenDeleteUseCase_51_,
    private val searchUseCase: GenSearchUseCase_51_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_51_>(GenState_51_.Idle)
    val state: StateFlow<GenState_51_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_51_) {
        when (event) {
            is GenEvent_51_.Load -> loadAll()
            is GenEvent_51_.Update -> save(event.model)
            is GenEvent_51_.Delete -> delete(event.id)
            is GenEvent_51_.Refresh -> loadAll()
            is GenEvent_51_.Search -> search(event.query)
            is GenEvent_51_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_51_.Loading; _state.value = GenState_51_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_51_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_51_.Success(searchUseCase(query)) } }
}
