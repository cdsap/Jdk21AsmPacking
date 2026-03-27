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

data class GenModel_1495_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1495_ {
    data class Load(val id: Long) : GenEvent_1495_()
    data class Update(val model: GenModel_1495_) : GenEvent_1495_()
    data class Delete(val id: Long) : GenEvent_1495_()
    data object Refresh : GenEvent_1495_()
    data class Search(val query: String) : GenEvent_1495_()
    data class Filter(val predicate: String) : GenEvent_1495_()
}

sealed class GenState_1495_ {
    data object Idle : GenState_1495_()
    data object Loading : GenState_1495_()
    data class Success(val items: List<GenModel_1495_>) : GenState_1495_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1495_()
    data class Partial(val items: List<GenModel_1495_>, val hasMore: Boolean) : GenState_1495_()
}

interface GenRepository_1495_ {
    suspend fun getAll(): List<GenModel_1495_>
    suspend fun getById(id: Long): GenModel_1495_?
    suspend fun save(model: GenModel_1495_): GenModel_1495_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1495_>
}

@Singleton
class GenRepositoryImpl_1495_ @Inject constructor() : GenRepository_1495_ {
    private val store = mutableMapOf<Long, GenModel_1495_>()
    override suspend fun getAll(): List<GenModel_1495_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1495_? = store[id]
    override suspend fun save(model: GenModel_1495_): GenModel_1495_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1495_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1495_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1495_ @Inject constructor(
    private val repository: GenRepositoryImpl_1495_
) : GenUseCase_1495_<Unit, List<GenModel_1495_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1495_> = repository.getAll()
}

class GenSaveUseCase_1495_ @Inject constructor(
    private val repository: GenRepositoryImpl_1495_
) : GenUseCase_1495_<GenModel_1495_, GenModel_1495_> {
    override suspend fun invoke(params: GenModel_1495_): GenModel_1495_ = repository.save(params)
}

class GenDeleteUseCase_1495_ @Inject constructor(
    private val repository: GenRepositoryImpl_1495_
) : GenUseCase_1495_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1495_ @Inject constructor(
    private val repository: GenRepositoryImpl_1495_
) : GenUseCase_1495_<String, List<GenModel_1495_>> {
    override suspend fun invoke(params: String): List<GenModel_1495_> = repository.search(params)
}

abstract class GenMapper_1495_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1495_ : GenMapper_1495_<GenModel_1495_, String>() {
    override fun map(input: GenModel_1495_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1495_ : GenMapper_1495_<String, GenModel_1495_>() {
    override fun map(input: String): GenModel_1495_ {
        val parts = input.split(":")
        return GenModel_1495_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1495_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1495_,
    private val saveUseCase: GenSaveUseCase_1495_,
    private val deleteUseCase: GenDeleteUseCase_1495_,
    private val searchUseCase: GenSearchUseCase_1495_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1495_>(GenState_1495_.Idle)
    val state: StateFlow<GenState_1495_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1495_) {
        when (event) {
            is GenEvent_1495_.Load -> loadAll()
            is GenEvent_1495_.Update -> save(event.model)
            is GenEvent_1495_.Delete -> delete(event.id)
            is GenEvent_1495_.Refresh -> loadAll()
            is GenEvent_1495_.Search -> search(event.query)
            is GenEvent_1495_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1495_.Loading; _state.value = GenState_1495_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1495_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1495_.Success(searchUseCase(query)) } }
}
