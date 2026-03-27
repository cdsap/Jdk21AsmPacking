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

data class GenModel_522_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_522_ {
    data class Load(val id: Long) : GenEvent_522_()
    data class Update(val model: GenModel_522_) : GenEvent_522_()
    data class Delete(val id: Long) : GenEvent_522_()
    data object Refresh : GenEvent_522_()
    data class Search(val query: String) : GenEvent_522_()
    data class Filter(val predicate: String) : GenEvent_522_()
}

sealed class GenState_522_ {
    data object Idle : GenState_522_()
    data object Loading : GenState_522_()
    data class Success(val items: List<GenModel_522_>) : GenState_522_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_522_()
    data class Partial(val items: List<GenModel_522_>, val hasMore: Boolean) : GenState_522_()
}

interface GenRepository_522_ {
    suspend fun getAll(): List<GenModel_522_>
    suspend fun getById(id: Long): GenModel_522_?
    suspend fun save(model: GenModel_522_): GenModel_522_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_522_>
}

@Singleton
class GenRepositoryImpl_522_ @Inject constructor() : GenRepository_522_ {
    private val store = mutableMapOf<Long, GenModel_522_>()
    override suspend fun getAll(): List<GenModel_522_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_522_? = store[id]
    override suspend fun save(model: GenModel_522_): GenModel_522_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_522_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_522_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_522_ @Inject constructor(
    private val repository: GenRepositoryImpl_522_
) : GenUseCase_522_<Unit, List<GenModel_522_>> {
    override suspend fun invoke(params: Unit): List<GenModel_522_> = repository.getAll()
}

class GenSaveUseCase_522_ @Inject constructor(
    private val repository: GenRepositoryImpl_522_
) : GenUseCase_522_<GenModel_522_, GenModel_522_> {
    override suspend fun invoke(params: GenModel_522_): GenModel_522_ = repository.save(params)
}

class GenDeleteUseCase_522_ @Inject constructor(
    private val repository: GenRepositoryImpl_522_
) : GenUseCase_522_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_522_ @Inject constructor(
    private val repository: GenRepositoryImpl_522_
) : GenUseCase_522_<String, List<GenModel_522_>> {
    override suspend fun invoke(params: String): List<GenModel_522_> = repository.search(params)
}

abstract class GenMapper_522_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_522_ : GenMapper_522_<GenModel_522_, String>() {
    override fun map(input: GenModel_522_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_522_ : GenMapper_522_<String, GenModel_522_>() {
    override fun map(input: String): GenModel_522_ {
        val parts = input.split(":")
        return GenModel_522_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_522_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_522_,
    private val saveUseCase: GenSaveUseCase_522_,
    private val deleteUseCase: GenDeleteUseCase_522_,
    private val searchUseCase: GenSearchUseCase_522_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_522_>(GenState_522_.Idle)
    val state: StateFlow<GenState_522_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_522_) {
        when (event) {
            is GenEvent_522_.Load -> loadAll()
            is GenEvent_522_.Update -> save(event.model)
            is GenEvent_522_.Delete -> delete(event.id)
            is GenEvent_522_.Refresh -> loadAll()
            is GenEvent_522_.Search -> search(event.query)
            is GenEvent_522_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_522_.Loading; _state.value = GenState_522_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_522_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_522_.Success(searchUseCase(query)) } }
}
