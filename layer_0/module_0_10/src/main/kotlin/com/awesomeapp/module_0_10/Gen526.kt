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

data class GenModel_526_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_526_ {
    data class Load(val id: Long) : GenEvent_526_()
    data class Update(val model: GenModel_526_) : GenEvent_526_()
    data class Delete(val id: Long) : GenEvent_526_()
    data object Refresh : GenEvent_526_()
    data class Search(val query: String) : GenEvent_526_()
    data class Filter(val predicate: String) : GenEvent_526_()
}

sealed class GenState_526_ {
    data object Idle : GenState_526_()
    data object Loading : GenState_526_()
    data class Success(val items: List<GenModel_526_>) : GenState_526_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_526_()
    data class Partial(val items: List<GenModel_526_>, val hasMore: Boolean) : GenState_526_()
}

interface GenRepository_526_ {
    suspend fun getAll(): List<GenModel_526_>
    suspend fun getById(id: Long): GenModel_526_?
    suspend fun save(model: GenModel_526_): GenModel_526_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_526_>
}

@Singleton
class GenRepositoryImpl_526_ @Inject constructor() : GenRepository_526_ {
    private val store = mutableMapOf<Long, GenModel_526_>()
    override suspend fun getAll(): List<GenModel_526_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_526_? = store[id]
    override suspend fun save(model: GenModel_526_): GenModel_526_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_526_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_526_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_526_ @Inject constructor(
    private val repository: GenRepositoryImpl_526_
) : GenUseCase_526_<Unit, List<GenModel_526_>> {
    override suspend fun invoke(params: Unit): List<GenModel_526_> = repository.getAll()
}

class GenSaveUseCase_526_ @Inject constructor(
    private val repository: GenRepositoryImpl_526_
) : GenUseCase_526_<GenModel_526_, GenModel_526_> {
    override suspend fun invoke(params: GenModel_526_): GenModel_526_ = repository.save(params)
}

class GenDeleteUseCase_526_ @Inject constructor(
    private val repository: GenRepositoryImpl_526_
) : GenUseCase_526_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_526_ @Inject constructor(
    private val repository: GenRepositoryImpl_526_
) : GenUseCase_526_<String, List<GenModel_526_>> {
    override suspend fun invoke(params: String): List<GenModel_526_> = repository.search(params)
}

abstract class GenMapper_526_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_526_ : GenMapper_526_<GenModel_526_, String>() {
    override fun map(input: GenModel_526_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_526_ : GenMapper_526_<String, GenModel_526_>() {
    override fun map(input: String): GenModel_526_ {
        val parts = input.split(":")
        return GenModel_526_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_526_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_526_,
    private val saveUseCase: GenSaveUseCase_526_,
    private val deleteUseCase: GenDeleteUseCase_526_,
    private val searchUseCase: GenSearchUseCase_526_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_526_>(GenState_526_.Idle)
    val state: StateFlow<GenState_526_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_526_) {
        when (event) {
            is GenEvent_526_.Load -> loadAll()
            is GenEvent_526_.Update -> save(event.model)
            is GenEvent_526_.Delete -> delete(event.id)
            is GenEvent_526_.Refresh -> loadAll()
            is GenEvent_526_.Search -> search(event.query)
            is GenEvent_526_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_526_.Loading; _state.value = GenState_526_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_526_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_526_.Success(searchUseCase(query)) } }
}
