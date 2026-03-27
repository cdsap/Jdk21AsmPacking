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

data class GenModel_4_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_4_ {
    data class Load(val id: Long) : GenEvent_4_()
    data class Update(val model: GenModel_4_) : GenEvent_4_()
    data class Delete(val id: Long) : GenEvent_4_()
    data object Refresh : GenEvent_4_()
    data class Search(val query: String) : GenEvent_4_()
    data class Filter(val predicate: String) : GenEvent_4_()
}

sealed class GenState_4_ {
    data object Idle : GenState_4_()
    data object Loading : GenState_4_()
    data class Success(val items: List<GenModel_4_>) : GenState_4_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_4_()
    data class Partial(val items: List<GenModel_4_>, val hasMore: Boolean) : GenState_4_()
}

interface GenRepository_4_ {
    suspend fun getAll(): List<GenModel_4_>
    suspend fun getById(id: Long): GenModel_4_?
    suspend fun save(model: GenModel_4_): GenModel_4_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_4_>
}

@Singleton
class GenRepositoryImpl_4_ @Inject constructor() : GenRepository_4_ {
    private val store = mutableMapOf<Long, GenModel_4_>()
    override suspend fun getAll(): List<GenModel_4_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_4_? = store[id]
    override suspend fun save(model: GenModel_4_): GenModel_4_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_4_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_4_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_4_ @Inject constructor(
    private val repository: GenRepositoryImpl_4_
) : GenUseCase_4_<Unit, List<GenModel_4_>> {
    override suspend fun invoke(params: Unit): List<GenModel_4_> = repository.getAll()
}

class GenSaveUseCase_4_ @Inject constructor(
    private val repository: GenRepositoryImpl_4_
) : GenUseCase_4_<GenModel_4_, GenModel_4_> {
    override suspend fun invoke(params: GenModel_4_): GenModel_4_ = repository.save(params)
}

class GenDeleteUseCase_4_ @Inject constructor(
    private val repository: GenRepositoryImpl_4_
) : GenUseCase_4_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_4_ @Inject constructor(
    private val repository: GenRepositoryImpl_4_
) : GenUseCase_4_<String, List<GenModel_4_>> {
    override suspend fun invoke(params: String): List<GenModel_4_> = repository.search(params)
}

abstract class GenMapper_4_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_4_ : GenMapper_4_<GenModel_4_, String>() {
    override fun map(input: GenModel_4_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_4_ : GenMapper_4_<String, GenModel_4_>() {
    override fun map(input: String): GenModel_4_ {
        val parts = input.split(":")
        return GenModel_4_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_4_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_4_,
    private val saveUseCase: GenSaveUseCase_4_,
    private val deleteUseCase: GenDeleteUseCase_4_,
    private val searchUseCase: GenSearchUseCase_4_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_4_>(GenState_4_.Idle)
    val state: StateFlow<GenState_4_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_4_) {
        when (event) {
            is GenEvent_4_.Load -> loadAll()
            is GenEvent_4_.Update -> save(event.model)
            is GenEvent_4_.Delete -> delete(event.id)
            is GenEvent_4_.Refresh -> loadAll()
            is GenEvent_4_.Search -> search(event.query)
            is GenEvent_4_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_4_.Loading; _state.value = GenState_4_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_4_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_4_.Success(searchUseCase(query)) } }
}
