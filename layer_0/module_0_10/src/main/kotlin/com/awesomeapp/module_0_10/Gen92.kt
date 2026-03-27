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

data class GenModel_92_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_92_ {
    data class Load(val id: Long) : GenEvent_92_()
    data class Update(val model: GenModel_92_) : GenEvent_92_()
    data class Delete(val id: Long) : GenEvent_92_()
    data object Refresh : GenEvent_92_()
    data class Search(val query: String) : GenEvent_92_()
    data class Filter(val predicate: String) : GenEvent_92_()
}

sealed class GenState_92_ {
    data object Idle : GenState_92_()
    data object Loading : GenState_92_()
    data class Success(val items: List<GenModel_92_>) : GenState_92_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_92_()
    data class Partial(val items: List<GenModel_92_>, val hasMore: Boolean) : GenState_92_()
}

interface GenRepository_92_ {
    suspend fun getAll(): List<GenModel_92_>
    suspend fun getById(id: Long): GenModel_92_?
    suspend fun save(model: GenModel_92_): GenModel_92_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_92_>
}

@Singleton
class GenRepositoryImpl_92_ @Inject constructor() : GenRepository_92_ {
    private val store = mutableMapOf<Long, GenModel_92_>()
    override suspend fun getAll(): List<GenModel_92_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_92_? = store[id]
    override suspend fun save(model: GenModel_92_): GenModel_92_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_92_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_92_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_92_ @Inject constructor(
    private val repository: GenRepositoryImpl_92_
) : GenUseCase_92_<Unit, List<GenModel_92_>> {
    override suspend fun invoke(params: Unit): List<GenModel_92_> = repository.getAll()
}

class GenSaveUseCase_92_ @Inject constructor(
    private val repository: GenRepositoryImpl_92_
) : GenUseCase_92_<GenModel_92_, GenModel_92_> {
    override suspend fun invoke(params: GenModel_92_): GenModel_92_ = repository.save(params)
}

class GenDeleteUseCase_92_ @Inject constructor(
    private val repository: GenRepositoryImpl_92_
) : GenUseCase_92_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_92_ @Inject constructor(
    private val repository: GenRepositoryImpl_92_
) : GenUseCase_92_<String, List<GenModel_92_>> {
    override suspend fun invoke(params: String): List<GenModel_92_> = repository.search(params)
}

abstract class GenMapper_92_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_92_ : GenMapper_92_<GenModel_92_, String>() {
    override fun map(input: GenModel_92_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_92_ : GenMapper_92_<String, GenModel_92_>() {
    override fun map(input: String): GenModel_92_ {
        val parts = input.split(":")
        return GenModel_92_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_92_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_92_,
    private val saveUseCase: GenSaveUseCase_92_,
    private val deleteUseCase: GenDeleteUseCase_92_,
    private val searchUseCase: GenSearchUseCase_92_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_92_>(GenState_92_.Idle)
    val state: StateFlow<GenState_92_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_92_) {
        when (event) {
            is GenEvent_92_.Load -> loadAll()
            is GenEvent_92_.Update -> save(event.model)
            is GenEvent_92_.Delete -> delete(event.id)
            is GenEvent_92_.Refresh -> loadAll()
            is GenEvent_92_.Search -> search(event.query)
            is GenEvent_92_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_92_.Loading; _state.value = GenState_92_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_92_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_92_.Success(searchUseCase(query)) } }
}
