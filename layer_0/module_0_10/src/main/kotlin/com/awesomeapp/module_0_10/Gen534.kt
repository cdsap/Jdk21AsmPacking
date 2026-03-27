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

data class GenModel_534_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_534_ {
    data class Load(val id: Long) : GenEvent_534_()
    data class Update(val model: GenModel_534_) : GenEvent_534_()
    data class Delete(val id: Long) : GenEvent_534_()
    data object Refresh : GenEvent_534_()
    data class Search(val query: String) : GenEvent_534_()
    data class Filter(val predicate: String) : GenEvent_534_()
}

sealed class GenState_534_ {
    data object Idle : GenState_534_()
    data object Loading : GenState_534_()
    data class Success(val items: List<GenModel_534_>) : GenState_534_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_534_()
    data class Partial(val items: List<GenModel_534_>, val hasMore: Boolean) : GenState_534_()
}

interface GenRepository_534_ {
    suspend fun getAll(): List<GenModel_534_>
    suspend fun getById(id: Long): GenModel_534_?
    suspend fun save(model: GenModel_534_): GenModel_534_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_534_>
}

@Singleton
class GenRepositoryImpl_534_ @Inject constructor() : GenRepository_534_ {
    private val store = mutableMapOf<Long, GenModel_534_>()
    override suspend fun getAll(): List<GenModel_534_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_534_? = store[id]
    override suspend fun save(model: GenModel_534_): GenModel_534_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_534_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_534_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_534_ @Inject constructor(
    private val repository: GenRepositoryImpl_534_
) : GenUseCase_534_<Unit, List<GenModel_534_>> {
    override suspend fun invoke(params: Unit): List<GenModel_534_> = repository.getAll()
}

class GenSaveUseCase_534_ @Inject constructor(
    private val repository: GenRepositoryImpl_534_
) : GenUseCase_534_<GenModel_534_, GenModel_534_> {
    override suspend fun invoke(params: GenModel_534_): GenModel_534_ = repository.save(params)
}

class GenDeleteUseCase_534_ @Inject constructor(
    private val repository: GenRepositoryImpl_534_
) : GenUseCase_534_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_534_ @Inject constructor(
    private val repository: GenRepositoryImpl_534_
) : GenUseCase_534_<String, List<GenModel_534_>> {
    override suspend fun invoke(params: String): List<GenModel_534_> = repository.search(params)
}

abstract class GenMapper_534_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_534_ : GenMapper_534_<GenModel_534_, String>() {
    override fun map(input: GenModel_534_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_534_ : GenMapper_534_<String, GenModel_534_>() {
    override fun map(input: String): GenModel_534_ {
        val parts = input.split(":")
        return GenModel_534_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_534_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_534_,
    private val saveUseCase: GenSaveUseCase_534_,
    private val deleteUseCase: GenDeleteUseCase_534_,
    private val searchUseCase: GenSearchUseCase_534_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_534_>(GenState_534_.Idle)
    val state: StateFlow<GenState_534_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_534_) {
        when (event) {
            is GenEvent_534_.Load -> loadAll()
            is GenEvent_534_.Update -> save(event.model)
            is GenEvent_534_.Delete -> delete(event.id)
            is GenEvent_534_.Refresh -> loadAll()
            is GenEvent_534_.Search -> search(event.query)
            is GenEvent_534_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_534_.Loading; _state.value = GenState_534_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_534_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_534_.Success(searchUseCase(query)) } }
}
