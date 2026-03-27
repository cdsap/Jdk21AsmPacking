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

data class GenModel_564_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_564_ {
    data class Load(val id: Long) : GenEvent_564_()
    data class Update(val model: GenModel_564_) : GenEvent_564_()
    data class Delete(val id: Long) : GenEvent_564_()
    data object Refresh : GenEvent_564_()
    data class Search(val query: String) : GenEvent_564_()
    data class Filter(val predicate: String) : GenEvent_564_()
}

sealed class GenState_564_ {
    data object Idle : GenState_564_()
    data object Loading : GenState_564_()
    data class Success(val items: List<GenModel_564_>) : GenState_564_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_564_()
    data class Partial(val items: List<GenModel_564_>, val hasMore: Boolean) : GenState_564_()
}

interface GenRepository_564_ {
    suspend fun getAll(): List<GenModel_564_>
    suspend fun getById(id: Long): GenModel_564_?
    suspend fun save(model: GenModel_564_): GenModel_564_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_564_>
}

@Singleton
class GenRepositoryImpl_564_ @Inject constructor() : GenRepository_564_ {
    private val store = mutableMapOf<Long, GenModel_564_>()
    override suspend fun getAll(): List<GenModel_564_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_564_? = store[id]
    override suspend fun save(model: GenModel_564_): GenModel_564_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_564_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_564_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_564_ @Inject constructor(
    private val repository: GenRepositoryImpl_564_
) : GenUseCase_564_<Unit, List<GenModel_564_>> {
    override suspend fun invoke(params: Unit): List<GenModel_564_> = repository.getAll()
}

class GenSaveUseCase_564_ @Inject constructor(
    private val repository: GenRepositoryImpl_564_
) : GenUseCase_564_<GenModel_564_, GenModel_564_> {
    override suspend fun invoke(params: GenModel_564_): GenModel_564_ = repository.save(params)
}

class GenDeleteUseCase_564_ @Inject constructor(
    private val repository: GenRepositoryImpl_564_
) : GenUseCase_564_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_564_ @Inject constructor(
    private val repository: GenRepositoryImpl_564_
) : GenUseCase_564_<String, List<GenModel_564_>> {
    override suspend fun invoke(params: String): List<GenModel_564_> = repository.search(params)
}

abstract class GenMapper_564_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_564_ : GenMapper_564_<GenModel_564_, String>() {
    override fun map(input: GenModel_564_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_564_ : GenMapper_564_<String, GenModel_564_>() {
    override fun map(input: String): GenModel_564_ {
        val parts = input.split(":")
        return GenModel_564_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_564_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_564_,
    private val saveUseCase: GenSaveUseCase_564_,
    private val deleteUseCase: GenDeleteUseCase_564_,
    private val searchUseCase: GenSearchUseCase_564_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_564_>(GenState_564_.Idle)
    val state: StateFlow<GenState_564_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_564_) {
        when (event) {
            is GenEvent_564_.Load -> loadAll()
            is GenEvent_564_.Update -> save(event.model)
            is GenEvent_564_.Delete -> delete(event.id)
            is GenEvent_564_.Refresh -> loadAll()
            is GenEvent_564_.Search -> search(event.query)
            is GenEvent_564_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_564_.Loading; _state.value = GenState_564_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_564_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_564_.Success(searchUseCase(query)) } }
}
