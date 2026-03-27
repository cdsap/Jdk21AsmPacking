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

data class GenModel_998_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_998_ {
    data class Load(val id: Long) : GenEvent_998_()
    data class Update(val model: GenModel_998_) : GenEvent_998_()
    data class Delete(val id: Long) : GenEvent_998_()
    data object Refresh : GenEvent_998_()
    data class Search(val query: String) : GenEvent_998_()
    data class Filter(val predicate: String) : GenEvent_998_()
}

sealed class GenState_998_ {
    data object Idle : GenState_998_()
    data object Loading : GenState_998_()
    data class Success(val items: List<GenModel_998_>) : GenState_998_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_998_()
    data class Partial(val items: List<GenModel_998_>, val hasMore: Boolean) : GenState_998_()
}

interface GenRepository_998_ {
    suspend fun getAll(): List<GenModel_998_>
    suspend fun getById(id: Long): GenModel_998_?
    suspend fun save(model: GenModel_998_): GenModel_998_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_998_>
}

@Singleton
class GenRepositoryImpl_998_ @Inject constructor() : GenRepository_998_ {
    private val store = mutableMapOf<Long, GenModel_998_>()
    override suspend fun getAll(): List<GenModel_998_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_998_? = store[id]
    override suspend fun save(model: GenModel_998_): GenModel_998_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_998_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_998_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_998_ @Inject constructor(
    private val repository: GenRepositoryImpl_998_
) : GenUseCase_998_<Unit, List<GenModel_998_>> {
    override suspend fun invoke(params: Unit): List<GenModel_998_> = repository.getAll()
}

class GenSaveUseCase_998_ @Inject constructor(
    private val repository: GenRepositoryImpl_998_
) : GenUseCase_998_<GenModel_998_, GenModel_998_> {
    override suspend fun invoke(params: GenModel_998_): GenModel_998_ = repository.save(params)
}

class GenDeleteUseCase_998_ @Inject constructor(
    private val repository: GenRepositoryImpl_998_
) : GenUseCase_998_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_998_ @Inject constructor(
    private val repository: GenRepositoryImpl_998_
) : GenUseCase_998_<String, List<GenModel_998_>> {
    override suspend fun invoke(params: String): List<GenModel_998_> = repository.search(params)
}

abstract class GenMapper_998_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_998_ : GenMapper_998_<GenModel_998_, String>() {
    override fun map(input: GenModel_998_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_998_ : GenMapper_998_<String, GenModel_998_>() {
    override fun map(input: String): GenModel_998_ {
        val parts = input.split(":")
        return GenModel_998_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_998_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_998_,
    private val saveUseCase: GenSaveUseCase_998_,
    private val deleteUseCase: GenDeleteUseCase_998_,
    private val searchUseCase: GenSearchUseCase_998_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_998_>(GenState_998_.Idle)
    val state: StateFlow<GenState_998_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_998_) {
        when (event) {
            is GenEvent_998_.Load -> loadAll()
            is GenEvent_998_.Update -> save(event.model)
            is GenEvent_998_.Delete -> delete(event.id)
            is GenEvent_998_.Refresh -> loadAll()
            is GenEvent_998_.Search -> search(event.query)
            is GenEvent_998_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_998_.Loading; _state.value = GenState_998_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_998_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_998_.Success(searchUseCase(query)) } }
}
