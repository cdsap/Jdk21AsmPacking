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

data class GenModel_673_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_673_ {
    data class Load(val id: Long) : GenEvent_673_()
    data class Update(val model: GenModel_673_) : GenEvent_673_()
    data class Delete(val id: Long) : GenEvent_673_()
    data object Refresh : GenEvent_673_()
    data class Search(val query: String) : GenEvent_673_()
    data class Filter(val predicate: String) : GenEvent_673_()
}

sealed class GenState_673_ {
    data object Idle : GenState_673_()
    data object Loading : GenState_673_()
    data class Success(val items: List<GenModel_673_>) : GenState_673_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_673_()
    data class Partial(val items: List<GenModel_673_>, val hasMore: Boolean) : GenState_673_()
}

interface GenRepository_673_ {
    suspend fun getAll(): List<GenModel_673_>
    suspend fun getById(id: Long): GenModel_673_?
    suspend fun save(model: GenModel_673_): GenModel_673_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_673_>
}

@Singleton
class GenRepositoryImpl_673_ @Inject constructor() : GenRepository_673_ {
    private val store = mutableMapOf<Long, GenModel_673_>()
    override suspend fun getAll(): List<GenModel_673_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_673_? = store[id]
    override suspend fun save(model: GenModel_673_): GenModel_673_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_673_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_673_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_673_ @Inject constructor(
    private val repository: GenRepositoryImpl_673_
) : GenUseCase_673_<Unit, List<GenModel_673_>> {
    override suspend fun invoke(params: Unit): List<GenModel_673_> = repository.getAll()
}

class GenSaveUseCase_673_ @Inject constructor(
    private val repository: GenRepositoryImpl_673_
) : GenUseCase_673_<GenModel_673_, GenModel_673_> {
    override suspend fun invoke(params: GenModel_673_): GenModel_673_ = repository.save(params)
}

class GenDeleteUseCase_673_ @Inject constructor(
    private val repository: GenRepositoryImpl_673_
) : GenUseCase_673_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_673_ @Inject constructor(
    private val repository: GenRepositoryImpl_673_
) : GenUseCase_673_<String, List<GenModel_673_>> {
    override suspend fun invoke(params: String): List<GenModel_673_> = repository.search(params)
}

abstract class GenMapper_673_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_673_ : GenMapper_673_<GenModel_673_, String>() {
    override fun map(input: GenModel_673_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_673_ : GenMapper_673_<String, GenModel_673_>() {
    override fun map(input: String): GenModel_673_ {
        val parts = input.split(":")
        return GenModel_673_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_673_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_673_,
    private val saveUseCase: GenSaveUseCase_673_,
    private val deleteUseCase: GenDeleteUseCase_673_,
    private val searchUseCase: GenSearchUseCase_673_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_673_>(GenState_673_.Idle)
    val state: StateFlow<GenState_673_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_673_) {
        when (event) {
            is GenEvent_673_.Load -> loadAll()
            is GenEvent_673_.Update -> save(event.model)
            is GenEvent_673_.Delete -> delete(event.id)
            is GenEvent_673_.Refresh -> loadAll()
            is GenEvent_673_.Search -> search(event.query)
            is GenEvent_673_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_673_.Loading; _state.value = GenState_673_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_673_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_673_.Success(searchUseCase(query)) } }
}
