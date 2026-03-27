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

data class GenModel_761_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_761_ {
    data class Load(val id: Long) : GenEvent_761_()
    data class Update(val model: GenModel_761_) : GenEvent_761_()
    data class Delete(val id: Long) : GenEvent_761_()
    data object Refresh : GenEvent_761_()
    data class Search(val query: String) : GenEvent_761_()
    data class Filter(val predicate: String) : GenEvent_761_()
}

sealed class GenState_761_ {
    data object Idle : GenState_761_()
    data object Loading : GenState_761_()
    data class Success(val items: List<GenModel_761_>) : GenState_761_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_761_()
    data class Partial(val items: List<GenModel_761_>, val hasMore: Boolean) : GenState_761_()
}

interface GenRepository_761_ {
    suspend fun getAll(): List<GenModel_761_>
    suspend fun getById(id: Long): GenModel_761_?
    suspend fun save(model: GenModel_761_): GenModel_761_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_761_>
}

@Singleton
class GenRepositoryImpl_761_ @Inject constructor() : GenRepository_761_ {
    private val store = mutableMapOf<Long, GenModel_761_>()
    override suspend fun getAll(): List<GenModel_761_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_761_? = store[id]
    override suspend fun save(model: GenModel_761_): GenModel_761_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_761_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_761_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_761_ @Inject constructor(
    private val repository: GenRepositoryImpl_761_
) : GenUseCase_761_<Unit, List<GenModel_761_>> {
    override suspend fun invoke(params: Unit): List<GenModel_761_> = repository.getAll()
}

class GenSaveUseCase_761_ @Inject constructor(
    private val repository: GenRepositoryImpl_761_
) : GenUseCase_761_<GenModel_761_, GenModel_761_> {
    override suspend fun invoke(params: GenModel_761_): GenModel_761_ = repository.save(params)
}

class GenDeleteUseCase_761_ @Inject constructor(
    private val repository: GenRepositoryImpl_761_
) : GenUseCase_761_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_761_ @Inject constructor(
    private val repository: GenRepositoryImpl_761_
) : GenUseCase_761_<String, List<GenModel_761_>> {
    override suspend fun invoke(params: String): List<GenModel_761_> = repository.search(params)
}

abstract class GenMapper_761_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_761_ : GenMapper_761_<GenModel_761_, String>() {
    override fun map(input: GenModel_761_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_761_ : GenMapper_761_<String, GenModel_761_>() {
    override fun map(input: String): GenModel_761_ {
        val parts = input.split(":")
        return GenModel_761_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_761_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_761_,
    private val saveUseCase: GenSaveUseCase_761_,
    private val deleteUseCase: GenDeleteUseCase_761_,
    private val searchUseCase: GenSearchUseCase_761_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_761_>(GenState_761_.Idle)
    val state: StateFlow<GenState_761_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_761_) {
        when (event) {
            is GenEvent_761_.Load -> loadAll()
            is GenEvent_761_.Update -> save(event.model)
            is GenEvent_761_.Delete -> delete(event.id)
            is GenEvent_761_.Refresh -> loadAll()
            is GenEvent_761_.Search -> search(event.query)
            is GenEvent_761_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_761_.Loading; _state.value = GenState_761_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_761_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_761_.Success(searchUseCase(query)) } }
}
