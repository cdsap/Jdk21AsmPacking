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

data class GenModel_76_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_76_ {
    data class Load(val id: Long) : GenEvent_76_()
    data class Update(val model: GenModel_76_) : GenEvent_76_()
    data class Delete(val id: Long) : GenEvent_76_()
    data object Refresh : GenEvent_76_()
    data class Search(val query: String) : GenEvent_76_()
    data class Filter(val predicate: String) : GenEvent_76_()
}

sealed class GenState_76_ {
    data object Idle : GenState_76_()
    data object Loading : GenState_76_()
    data class Success(val items: List<GenModel_76_>) : GenState_76_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_76_()
    data class Partial(val items: List<GenModel_76_>, val hasMore: Boolean) : GenState_76_()
}

interface GenRepository_76_ {
    suspend fun getAll(): List<GenModel_76_>
    suspend fun getById(id: Long): GenModel_76_?
    suspend fun save(model: GenModel_76_): GenModel_76_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_76_>
}

@Singleton
class GenRepositoryImpl_76_ @Inject constructor() : GenRepository_76_ {
    private val store = mutableMapOf<Long, GenModel_76_>()
    override suspend fun getAll(): List<GenModel_76_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_76_? = store[id]
    override suspend fun save(model: GenModel_76_): GenModel_76_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_76_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_76_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_76_ @Inject constructor(
    private val repository: GenRepositoryImpl_76_
) : GenUseCase_76_<Unit, List<GenModel_76_>> {
    override suspend fun invoke(params: Unit): List<GenModel_76_> = repository.getAll()
}

class GenSaveUseCase_76_ @Inject constructor(
    private val repository: GenRepositoryImpl_76_
) : GenUseCase_76_<GenModel_76_, GenModel_76_> {
    override suspend fun invoke(params: GenModel_76_): GenModel_76_ = repository.save(params)
}

class GenDeleteUseCase_76_ @Inject constructor(
    private val repository: GenRepositoryImpl_76_
) : GenUseCase_76_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_76_ @Inject constructor(
    private val repository: GenRepositoryImpl_76_
) : GenUseCase_76_<String, List<GenModel_76_>> {
    override suspend fun invoke(params: String): List<GenModel_76_> = repository.search(params)
}

abstract class GenMapper_76_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_76_ : GenMapper_76_<GenModel_76_, String>() {
    override fun map(input: GenModel_76_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_76_ : GenMapper_76_<String, GenModel_76_>() {
    override fun map(input: String): GenModel_76_ {
        val parts = input.split(":")
        return GenModel_76_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_76_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_76_,
    private val saveUseCase: GenSaveUseCase_76_,
    private val deleteUseCase: GenDeleteUseCase_76_,
    private val searchUseCase: GenSearchUseCase_76_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_76_>(GenState_76_.Idle)
    val state: StateFlow<GenState_76_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_76_) {
        when (event) {
            is GenEvent_76_.Load -> loadAll()
            is GenEvent_76_.Update -> save(event.model)
            is GenEvent_76_.Delete -> delete(event.id)
            is GenEvent_76_.Refresh -> loadAll()
            is GenEvent_76_.Search -> search(event.query)
            is GenEvent_76_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_76_.Loading; _state.value = GenState_76_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_76_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_76_.Success(searchUseCase(query)) } }
}
