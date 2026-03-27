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

data class GenModel_38_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_38_ {
    data class Load(val id: Long) : GenEvent_38_()
    data class Update(val model: GenModel_38_) : GenEvent_38_()
    data class Delete(val id: Long) : GenEvent_38_()
    data object Refresh : GenEvent_38_()
    data class Search(val query: String) : GenEvent_38_()
    data class Filter(val predicate: String) : GenEvent_38_()
}

sealed class GenState_38_ {
    data object Idle : GenState_38_()
    data object Loading : GenState_38_()
    data class Success(val items: List<GenModel_38_>) : GenState_38_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_38_()
    data class Partial(val items: List<GenModel_38_>, val hasMore: Boolean) : GenState_38_()
}

interface GenRepository_38_ {
    suspend fun getAll(): List<GenModel_38_>
    suspend fun getById(id: Long): GenModel_38_?
    suspend fun save(model: GenModel_38_): GenModel_38_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_38_>
}

@Singleton
class GenRepositoryImpl_38_ @Inject constructor() : GenRepository_38_ {
    private val store = mutableMapOf<Long, GenModel_38_>()
    override suspend fun getAll(): List<GenModel_38_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_38_? = store[id]
    override suspend fun save(model: GenModel_38_): GenModel_38_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_38_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_38_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_38_ @Inject constructor(
    private val repository: GenRepositoryImpl_38_
) : GenUseCase_38_<Unit, List<GenModel_38_>> {
    override suspend fun invoke(params: Unit): List<GenModel_38_> = repository.getAll()
}

class GenSaveUseCase_38_ @Inject constructor(
    private val repository: GenRepositoryImpl_38_
) : GenUseCase_38_<GenModel_38_, GenModel_38_> {
    override suspend fun invoke(params: GenModel_38_): GenModel_38_ = repository.save(params)
}

class GenDeleteUseCase_38_ @Inject constructor(
    private val repository: GenRepositoryImpl_38_
) : GenUseCase_38_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_38_ @Inject constructor(
    private val repository: GenRepositoryImpl_38_
) : GenUseCase_38_<String, List<GenModel_38_>> {
    override suspend fun invoke(params: String): List<GenModel_38_> = repository.search(params)
}

abstract class GenMapper_38_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_38_ : GenMapper_38_<GenModel_38_, String>() {
    override fun map(input: GenModel_38_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_38_ : GenMapper_38_<String, GenModel_38_>() {
    override fun map(input: String): GenModel_38_ {
        val parts = input.split(":")
        return GenModel_38_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_38_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_38_,
    private val saveUseCase: GenSaveUseCase_38_,
    private val deleteUseCase: GenDeleteUseCase_38_,
    private val searchUseCase: GenSearchUseCase_38_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_38_>(GenState_38_.Idle)
    val state: StateFlow<GenState_38_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_38_) {
        when (event) {
            is GenEvent_38_.Load -> loadAll()
            is GenEvent_38_.Update -> save(event.model)
            is GenEvent_38_.Delete -> delete(event.id)
            is GenEvent_38_.Refresh -> loadAll()
            is GenEvent_38_.Search -> search(event.query)
            is GenEvent_38_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_38_.Loading; _state.value = GenState_38_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_38_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_38_.Success(searchUseCase(query)) } }
}
