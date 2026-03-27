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

data class GenModel_1192_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1192_ {
    data class Load(val id: Long) : GenEvent_1192_()
    data class Update(val model: GenModel_1192_) : GenEvent_1192_()
    data class Delete(val id: Long) : GenEvent_1192_()
    data object Refresh : GenEvent_1192_()
    data class Search(val query: String) : GenEvent_1192_()
    data class Filter(val predicate: String) : GenEvent_1192_()
}

sealed class GenState_1192_ {
    data object Idle : GenState_1192_()
    data object Loading : GenState_1192_()
    data class Success(val items: List<GenModel_1192_>) : GenState_1192_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1192_()
    data class Partial(val items: List<GenModel_1192_>, val hasMore: Boolean) : GenState_1192_()
}

interface GenRepository_1192_ {
    suspend fun getAll(): List<GenModel_1192_>
    suspend fun getById(id: Long): GenModel_1192_?
    suspend fun save(model: GenModel_1192_): GenModel_1192_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1192_>
}

@Singleton
class GenRepositoryImpl_1192_ @Inject constructor() : GenRepository_1192_ {
    private val store = mutableMapOf<Long, GenModel_1192_>()
    override suspend fun getAll(): List<GenModel_1192_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1192_? = store[id]
    override suspend fun save(model: GenModel_1192_): GenModel_1192_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1192_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1192_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1192_ @Inject constructor(
    private val repository: GenRepositoryImpl_1192_
) : GenUseCase_1192_<Unit, List<GenModel_1192_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1192_> = repository.getAll()
}

class GenSaveUseCase_1192_ @Inject constructor(
    private val repository: GenRepositoryImpl_1192_
) : GenUseCase_1192_<GenModel_1192_, GenModel_1192_> {
    override suspend fun invoke(params: GenModel_1192_): GenModel_1192_ = repository.save(params)
}

class GenDeleteUseCase_1192_ @Inject constructor(
    private val repository: GenRepositoryImpl_1192_
) : GenUseCase_1192_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1192_ @Inject constructor(
    private val repository: GenRepositoryImpl_1192_
) : GenUseCase_1192_<String, List<GenModel_1192_>> {
    override suspend fun invoke(params: String): List<GenModel_1192_> = repository.search(params)
}

abstract class GenMapper_1192_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1192_ : GenMapper_1192_<GenModel_1192_, String>() {
    override fun map(input: GenModel_1192_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1192_ : GenMapper_1192_<String, GenModel_1192_>() {
    override fun map(input: String): GenModel_1192_ {
        val parts = input.split(":")
        return GenModel_1192_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1192_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1192_,
    private val saveUseCase: GenSaveUseCase_1192_,
    private val deleteUseCase: GenDeleteUseCase_1192_,
    private val searchUseCase: GenSearchUseCase_1192_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1192_>(GenState_1192_.Idle)
    val state: StateFlow<GenState_1192_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1192_) {
        when (event) {
            is GenEvent_1192_.Load -> loadAll()
            is GenEvent_1192_.Update -> save(event.model)
            is GenEvent_1192_.Delete -> delete(event.id)
            is GenEvent_1192_.Refresh -> loadAll()
            is GenEvent_1192_.Search -> search(event.query)
            is GenEvent_1192_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1192_.Loading; _state.value = GenState_1192_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1192_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1192_.Success(searchUseCase(query)) } }
}
