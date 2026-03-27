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

data class GenModel_59_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_59_ {
    data class Load(val id: Long) : GenEvent_59_()
    data class Update(val model: GenModel_59_) : GenEvent_59_()
    data class Delete(val id: Long) : GenEvent_59_()
    data object Refresh : GenEvent_59_()
    data class Search(val query: String) : GenEvent_59_()
    data class Filter(val predicate: String) : GenEvent_59_()
}

sealed class GenState_59_ {
    data object Idle : GenState_59_()
    data object Loading : GenState_59_()
    data class Success(val items: List<GenModel_59_>) : GenState_59_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_59_()
    data class Partial(val items: List<GenModel_59_>, val hasMore: Boolean) : GenState_59_()
}

interface GenRepository_59_ {
    suspend fun getAll(): List<GenModel_59_>
    suspend fun getById(id: Long): GenModel_59_?
    suspend fun save(model: GenModel_59_): GenModel_59_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_59_>
}

@Singleton
class GenRepositoryImpl_59_ @Inject constructor() : GenRepository_59_ {
    private val store = mutableMapOf<Long, GenModel_59_>()
    override suspend fun getAll(): List<GenModel_59_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_59_? = store[id]
    override suspend fun save(model: GenModel_59_): GenModel_59_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_59_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_59_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_59_ @Inject constructor(
    private val repository: GenRepositoryImpl_59_
) : GenUseCase_59_<Unit, List<GenModel_59_>> {
    override suspend fun invoke(params: Unit): List<GenModel_59_> = repository.getAll()
}

class GenSaveUseCase_59_ @Inject constructor(
    private val repository: GenRepositoryImpl_59_
) : GenUseCase_59_<GenModel_59_, GenModel_59_> {
    override suspend fun invoke(params: GenModel_59_): GenModel_59_ = repository.save(params)
}

class GenDeleteUseCase_59_ @Inject constructor(
    private val repository: GenRepositoryImpl_59_
) : GenUseCase_59_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_59_ @Inject constructor(
    private val repository: GenRepositoryImpl_59_
) : GenUseCase_59_<String, List<GenModel_59_>> {
    override suspend fun invoke(params: String): List<GenModel_59_> = repository.search(params)
}

abstract class GenMapper_59_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_59_ : GenMapper_59_<GenModel_59_, String>() {
    override fun map(input: GenModel_59_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_59_ : GenMapper_59_<String, GenModel_59_>() {
    override fun map(input: String): GenModel_59_ {
        val parts = input.split(":")
        return GenModel_59_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_59_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_59_,
    private val saveUseCase: GenSaveUseCase_59_,
    private val deleteUseCase: GenDeleteUseCase_59_,
    private val searchUseCase: GenSearchUseCase_59_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_59_>(GenState_59_.Idle)
    val state: StateFlow<GenState_59_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_59_) {
        when (event) {
            is GenEvent_59_.Load -> loadAll()
            is GenEvent_59_.Update -> save(event.model)
            is GenEvent_59_.Delete -> delete(event.id)
            is GenEvent_59_.Refresh -> loadAll()
            is GenEvent_59_.Search -> search(event.query)
            is GenEvent_59_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_59_.Loading; _state.value = GenState_59_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_59_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_59_.Success(searchUseCase(query)) } }
}
