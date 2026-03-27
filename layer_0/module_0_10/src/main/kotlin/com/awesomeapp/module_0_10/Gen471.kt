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

data class GenModel_471_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_471_ {
    data class Load(val id: Long) : GenEvent_471_()
    data class Update(val model: GenModel_471_) : GenEvent_471_()
    data class Delete(val id: Long) : GenEvent_471_()
    data object Refresh : GenEvent_471_()
    data class Search(val query: String) : GenEvent_471_()
    data class Filter(val predicate: String) : GenEvent_471_()
}

sealed class GenState_471_ {
    data object Idle : GenState_471_()
    data object Loading : GenState_471_()
    data class Success(val items: List<GenModel_471_>) : GenState_471_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_471_()
    data class Partial(val items: List<GenModel_471_>, val hasMore: Boolean) : GenState_471_()
}

interface GenRepository_471_ {
    suspend fun getAll(): List<GenModel_471_>
    suspend fun getById(id: Long): GenModel_471_?
    suspend fun save(model: GenModel_471_): GenModel_471_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_471_>
}

@Singleton
class GenRepositoryImpl_471_ @Inject constructor() : GenRepository_471_ {
    private val store = mutableMapOf<Long, GenModel_471_>()
    override suspend fun getAll(): List<GenModel_471_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_471_? = store[id]
    override suspend fun save(model: GenModel_471_): GenModel_471_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_471_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_471_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_471_ @Inject constructor(
    private val repository: GenRepositoryImpl_471_
) : GenUseCase_471_<Unit, List<GenModel_471_>> {
    override suspend fun invoke(params: Unit): List<GenModel_471_> = repository.getAll()
}

class GenSaveUseCase_471_ @Inject constructor(
    private val repository: GenRepositoryImpl_471_
) : GenUseCase_471_<GenModel_471_, GenModel_471_> {
    override suspend fun invoke(params: GenModel_471_): GenModel_471_ = repository.save(params)
}

class GenDeleteUseCase_471_ @Inject constructor(
    private val repository: GenRepositoryImpl_471_
) : GenUseCase_471_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_471_ @Inject constructor(
    private val repository: GenRepositoryImpl_471_
) : GenUseCase_471_<String, List<GenModel_471_>> {
    override suspend fun invoke(params: String): List<GenModel_471_> = repository.search(params)
}

abstract class GenMapper_471_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_471_ : GenMapper_471_<GenModel_471_, String>() {
    override fun map(input: GenModel_471_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_471_ : GenMapper_471_<String, GenModel_471_>() {
    override fun map(input: String): GenModel_471_ {
        val parts = input.split(":")
        return GenModel_471_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_471_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_471_,
    private val saveUseCase: GenSaveUseCase_471_,
    private val deleteUseCase: GenDeleteUseCase_471_,
    private val searchUseCase: GenSearchUseCase_471_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_471_>(GenState_471_.Idle)
    val state: StateFlow<GenState_471_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_471_) {
        when (event) {
            is GenEvent_471_.Load -> loadAll()
            is GenEvent_471_.Update -> save(event.model)
            is GenEvent_471_.Delete -> delete(event.id)
            is GenEvent_471_.Refresh -> loadAll()
            is GenEvent_471_.Search -> search(event.query)
            is GenEvent_471_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_471_.Loading; _state.value = GenState_471_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_471_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_471_.Success(searchUseCase(query)) } }
}
