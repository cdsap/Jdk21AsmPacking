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

data class GenModel_464_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_464_ {
    data class Load(val id: Long) : GenEvent_464_()
    data class Update(val model: GenModel_464_) : GenEvent_464_()
    data class Delete(val id: Long) : GenEvent_464_()
    data object Refresh : GenEvent_464_()
    data class Search(val query: String) : GenEvent_464_()
    data class Filter(val predicate: String) : GenEvent_464_()
}

sealed class GenState_464_ {
    data object Idle : GenState_464_()
    data object Loading : GenState_464_()
    data class Success(val items: List<GenModel_464_>) : GenState_464_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_464_()
    data class Partial(val items: List<GenModel_464_>, val hasMore: Boolean) : GenState_464_()
}

interface GenRepository_464_ {
    suspend fun getAll(): List<GenModel_464_>
    suspend fun getById(id: Long): GenModel_464_?
    suspend fun save(model: GenModel_464_): GenModel_464_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_464_>
}

@Singleton
class GenRepositoryImpl_464_ @Inject constructor() : GenRepository_464_ {
    private val store = mutableMapOf<Long, GenModel_464_>()
    override suspend fun getAll(): List<GenModel_464_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_464_? = store[id]
    override suspend fun save(model: GenModel_464_): GenModel_464_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_464_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_464_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_464_ @Inject constructor(
    private val repository: GenRepositoryImpl_464_
) : GenUseCase_464_<Unit, List<GenModel_464_>> {
    override suspend fun invoke(params: Unit): List<GenModel_464_> = repository.getAll()
}

class GenSaveUseCase_464_ @Inject constructor(
    private val repository: GenRepositoryImpl_464_
) : GenUseCase_464_<GenModel_464_, GenModel_464_> {
    override suspend fun invoke(params: GenModel_464_): GenModel_464_ = repository.save(params)
}

class GenDeleteUseCase_464_ @Inject constructor(
    private val repository: GenRepositoryImpl_464_
) : GenUseCase_464_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_464_ @Inject constructor(
    private val repository: GenRepositoryImpl_464_
) : GenUseCase_464_<String, List<GenModel_464_>> {
    override suspend fun invoke(params: String): List<GenModel_464_> = repository.search(params)
}

abstract class GenMapper_464_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_464_ : GenMapper_464_<GenModel_464_, String>() {
    override fun map(input: GenModel_464_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_464_ : GenMapper_464_<String, GenModel_464_>() {
    override fun map(input: String): GenModel_464_ {
        val parts = input.split(":")
        return GenModel_464_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_464_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_464_,
    private val saveUseCase: GenSaveUseCase_464_,
    private val deleteUseCase: GenDeleteUseCase_464_,
    private val searchUseCase: GenSearchUseCase_464_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_464_>(GenState_464_.Idle)
    val state: StateFlow<GenState_464_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_464_) {
        when (event) {
            is GenEvent_464_.Load -> loadAll()
            is GenEvent_464_.Update -> save(event.model)
            is GenEvent_464_.Delete -> delete(event.id)
            is GenEvent_464_.Refresh -> loadAll()
            is GenEvent_464_.Search -> search(event.query)
            is GenEvent_464_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_464_.Loading; _state.value = GenState_464_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_464_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_464_.Success(searchUseCase(query)) } }
}
