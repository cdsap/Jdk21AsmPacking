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

data class GenModel_3019_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3019_ {
    data class Load(val id: Long) : GenEvent_3019_()
    data class Update(val model: GenModel_3019_) : GenEvent_3019_()
    data class Delete(val id: Long) : GenEvent_3019_()
    data object Refresh : GenEvent_3019_()
    data class Search(val query: String) : GenEvent_3019_()
    data class Filter(val predicate: String) : GenEvent_3019_()
}

sealed class GenState_3019_ {
    data object Idle : GenState_3019_()
    data object Loading : GenState_3019_()
    data class Success(val items: List<GenModel_3019_>) : GenState_3019_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3019_()
    data class Partial(val items: List<GenModel_3019_>, val hasMore: Boolean) : GenState_3019_()
}

interface GenRepository_3019_ {
    suspend fun getAll(): List<GenModel_3019_>
    suspend fun getById(id: Long): GenModel_3019_?
    suspend fun save(model: GenModel_3019_): GenModel_3019_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3019_>
}

@Singleton
class GenRepositoryImpl_3019_ @Inject constructor() : GenRepository_3019_ {
    private val store = mutableMapOf<Long, GenModel_3019_>()
    override suspend fun getAll(): List<GenModel_3019_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3019_? = store[id]
    override suspend fun save(model: GenModel_3019_): GenModel_3019_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3019_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3019_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3019_ @Inject constructor(
    private val repository: GenRepositoryImpl_3019_
) : GenUseCase_3019_<Unit, List<GenModel_3019_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3019_> = repository.getAll()
}

class GenSaveUseCase_3019_ @Inject constructor(
    private val repository: GenRepositoryImpl_3019_
) : GenUseCase_3019_<GenModel_3019_, GenModel_3019_> {
    override suspend fun invoke(params: GenModel_3019_): GenModel_3019_ = repository.save(params)
}

class GenDeleteUseCase_3019_ @Inject constructor(
    private val repository: GenRepositoryImpl_3019_
) : GenUseCase_3019_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3019_ @Inject constructor(
    private val repository: GenRepositoryImpl_3019_
) : GenUseCase_3019_<String, List<GenModel_3019_>> {
    override suspend fun invoke(params: String): List<GenModel_3019_> = repository.search(params)
}

abstract class GenMapper_3019_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3019_ : GenMapper_3019_<GenModel_3019_, String>() {
    override fun map(input: GenModel_3019_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3019_ : GenMapper_3019_<String, GenModel_3019_>() {
    override fun map(input: String): GenModel_3019_ {
        val parts = input.split(":")
        return GenModel_3019_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3019_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3019_,
    private val saveUseCase: GenSaveUseCase_3019_,
    private val deleteUseCase: GenDeleteUseCase_3019_,
    private val searchUseCase: GenSearchUseCase_3019_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3019_>(GenState_3019_.Idle)
    val state: StateFlow<GenState_3019_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3019_) {
        when (event) {
            is GenEvent_3019_.Load -> loadAll()
            is GenEvent_3019_.Update -> save(event.model)
            is GenEvent_3019_.Delete -> delete(event.id)
            is GenEvent_3019_.Refresh -> loadAll()
            is GenEvent_3019_.Search -> search(event.query)
            is GenEvent_3019_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3019_.Loading; _state.value = GenState_3019_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3019_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3019_.Success(searchUseCase(query)) } }
}
