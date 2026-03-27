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

data class GenModel_495_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_495_ {
    data class Load(val id: Long) : GenEvent_495_()
    data class Update(val model: GenModel_495_) : GenEvent_495_()
    data class Delete(val id: Long) : GenEvent_495_()
    data object Refresh : GenEvent_495_()
    data class Search(val query: String) : GenEvent_495_()
    data class Filter(val predicate: String) : GenEvent_495_()
}

sealed class GenState_495_ {
    data object Idle : GenState_495_()
    data object Loading : GenState_495_()
    data class Success(val items: List<GenModel_495_>) : GenState_495_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_495_()
    data class Partial(val items: List<GenModel_495_>, val hasMore: Boolean) : GenState_495_()
}

interface GenRepository_495_ {
    suspend fun getAll(): List<GenModel_495_>
    suspend fun getById(id: Long): GenModel_495_?
    suspend fun save(model: GenModel_495_): GenModel_495_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_495_>
}

@Singleton
class GenRepositoryImpl_495_ @Inject constructor() : GenRepository_495_ {
    private val store = mutableMapOf<Long, GenModel_495_>()
    override suspend fun getAll(): List<GenModel_495_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_495_? = store[id]
    override suspend fun save(model: GenModel_495_): GenModel_495_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_495_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_495_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_495_ @Inject constructor(
    private val repository: GenRepositoryImpl_495_
) : GenUseCase_495_<Unit, List<GenModel_495_>> {
    override suspend fun invoke(params: Unit): List<GenModel_495_> = repository.getAll()
}

class GenSaveUseCase_495_ @Inject constructor(
    private val repository: GenRepositoryImpl_495_
) : GenUseCase_495_<GenModel_495_, GenModel_495_> {
    override suspend fun invoke(params: GenModel_495_): GenModel_495_ = repository.save(params)
}

class GenDeleteUseCase_495_ @Inject constructor(
    private val repository: GenRepositoryImpl_495_
) : GenUseCase_495_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_495_ @Inject constructor(
    private val repository: GenRepositoryImpl_495_
) : GenUseCase_495_<String, List<GenModel_495_>> {
    override suspend fun invoke(params: String): List<GenModel_495_> = repository.search(params)
}

abstract class GenMapper_495_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_495_ : GenMapper_495_<GenModel_495_, String>() {
    override fun map(input: GenModel_495_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_495_ : GenMapper_495_<String, GenModel_495_>() {
    override fun map(input: String): GenModel_495_ {
        val parts = input.split(":")
        return GenModel_495_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_495_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_495_,
    private val saveUseCase: GenSaveUseCase_495_,
    private val deleteUseCase: GenDeleteUseCase_495_,
    private val searchUseCase: GenSearchUseCase_495_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_495_>(GenState_495_.Idle)
    val state: StateFlow<GenState_495_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_495_) {
        when (event) {
            is GenEvent_495_.Load -> loadAll()
            is GenEvent_495_.Update -> save(event.model)
            is GenEvent_495_.Delete -> delete(event.id)
            is GenEvent_495_.Refresh -> loadAll()
            is GenEvent_495_.Search -> search(event.query)
            is GenEvent_495_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_495_.Loading; _state.value = GenState_495_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_495_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_495_.Success(searchUseCase(query)) } }
}
