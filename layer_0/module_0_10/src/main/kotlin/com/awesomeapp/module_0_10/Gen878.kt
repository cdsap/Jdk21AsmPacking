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

data class GenModel_878_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_878_ {
    data class Load(val id: Long) : GenEvent_878_()
    data class Update(val model: GenModel_878_) : GenEvent_878_()
    data class Delete(val id: Long) : GenEvent_878_()
    data object Refresh : GenEvent_878_()
    data class Search(val query: String) : GenEvent_878_()
    data class Filter(val predicate: String) : GenEvent_878_()
}

sealed class GenState_878_ {
    data object Idle : GenState_878_()
    data object Loading : GenState_878_()
    data class Success(val items: List<GenModel_878_>) : GenState_878_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_878_()
    data class Partial(val items: List<GenModel_878_>, val hasMore: Boolean) : GenState_878_()
}

interface GenRepository_878_ {
    suspend fun getAll(): List<GenModel_878_>
    suspend fun getById(id: Long): GenModel_878_?
    suspend fun save(model: GenModel_878_): GenModel_878_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_878_>
}

@Singleton
class GenRepositoryImpl_878_ @Inject constructor() : GenRepository_878_ {
    private val store = mutableMapOf<Long, GenModel_878_>()
    override suspend fun getAll(): List<GenModel_878_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_878_? = store[id]
    override suspend fun save(model: GenModel_878_): GenModel_878_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_878_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_878_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_878_ @Inject constructor(
    private val repository: GenRepositoryImpl_878_
) : GenUseCase_878_<Unit, List<GenModel_878_>> {
    override suspend fun invoke(params: Unit): List<GenModel_878_> = repository.getAll()
}

class GenSaveUseCase_878_ @Inject constructor(
    private val repository: GenRepositoryImpl_878_
) : GenUseCase_878_<GenModel_878_, GenModel_878_> {
    override suspend fun invoke(params: GenModel_878_): GenModel_878_ = repository.save(params)
}

class GenDeleteUseCase_878_ @Inject constructor(
    private val repository: GenRepositoryImpl_878_
) : GenUseCase_878_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_878_ @Inject constructor(
    private val repository: GenRepositoryImpl_878_
) : GenUseCase_878_<String, List<GenModel_878_>> {
    override suspend fun invoke(params: String): List<GenModel_878_> = repository.search(params)
}

abstract class GenMapper_878_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_878_ : GenMapper_878_<GenModel_878_, String>() {
    override fun map(input: GenModel_878_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_878_ : GenMapper_878_<String, GenModel_878_>() {
    override fun map(input: String): GenModel_878_ {
        val parts = input.split(":")
        return GenModel_878_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_878_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_878_,
    private val saveUseCase: GenSaveUseCase_878_,
    private val deleteUseCase: GenDeleteUseCase_878_,
    private val searchUseCase: GenSearchUseCase_878_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_878_>(GenState_878_.Idle)
    val state: StateFlow<GenState_878_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_878_) {
        when (event) {
            is GenEvent_878_.Load -> loadAll()
            is GenEvent_878_.Update -> save(event.model)
            is GenEvent_878_.Delete -> delete(event.id)
            is GenEvent_878_.Refresh -> loadAll()
            is GenEvent_878_.Search -> search(event.query)
            is GenEvent_878_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_878_.Loading; _state.value = GenState_878_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_878_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_878_.Success(searchUseCase(query)) } }
}
