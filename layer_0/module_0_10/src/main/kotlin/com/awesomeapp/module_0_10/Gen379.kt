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

data class GenModel_379_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_379_ {
    data class Load(val id: Long) : GenEvent_379_()
    data class Update(val model: GenModel_379_) : GenEvent_379_()
    data class Delete(val id: Long) : GenEvent_379_()
    data object Refresh : GenEvent_379_()
    data class Search(val query: String) : GenEvent_379_()
    data class Filter(val predicate: String) : GenEvent_379_()
}

sealed class GenState_379_ {
    data object Idle : GenState_379_()
    data object Loading : GenState_379_()
    data class Success(val items: List<GenModel_379_>) : GenState_379_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_379_()
    data class Partial(val items: List<GenModel_379_>, val hasMore: Boolean) : GenState_379_()
}

interface GenRepository_379_ {
    suspend fun getAll(): List<GenModel_379_>
    suspend fun getById(id: Long): GenModel_379_?
    suspend fun save(model: GenModel_379_): GenModel_379_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_379_>
}

@Singleton
class GenRepositoryImpl_379_ @Inject constructor() : GenRepository_379_ {
    private val store = mutableMapOf<Long, GenModel_379_>()
    override suspend fun getAll(): List<GenModel_379_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_379_? = store[id]
    override suspend fun save(model: GenModel_379_): GenModel_379_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_379_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_379_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_379_ @Inject constructor(
    private val repository: GenRepositoryImpl_379_
) : GenUseCase_379_<Unit, List<GenModel_379_>> {
    override suspend fun invoke(params: Unit): List<GenModel_379_> = repository.getAll()
}

class GenSaveUseCase_379_ @Inject constructor(
    private val repository: GenRepositoryImpl_379_
) : GenUseCase_379_<GenModel_379_, GenModel_379_> {
    override suspend fun invoke(params: GenModel_379_): GenModel_379_ = repository.save(params)
}

class GenDeleteUseCase_379_ @Inject constructor(
    private val repository: GenRepositoryImpl_379_
) : GenUseCase_379_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_379_ @Inject constructor(
    private val repository: GenRepositoryImpl_379_
) : GenUseCase_379_<String, List<GenModel_379_>> {
    override suspend fun invoke(params: String): List<GenModel_379_> = repository.search(params)
}

abstract class GenMapper_379_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_379_ : GenMapper_379_<GenModel_379_, String>() {
    override fun map(input: GenModel_379_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_379_ : GenMapper_379_<String, GenModel_379_>() {
    override fun map(input: String): GenModel_379_ {
        val parts = input.split(":")
        return GenModel_379_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_379_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_379_,
    private val saveUseCase: GenSaveUseCase_379_,
    private val deleteUseCase: GenDeleteUseCase_379_,
    private val searchUseCase: GenSearchUseCase_379_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_379_>(GenState_379_.Idle)
    val state: StateFlow<GenState_379_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_379_) {
        when (event) {
            is GenEvent_379_.Load -> loadAll()
            is GenEvent_379_.Update -> save(event.model)
            is GenEvent_379_.Delete -> delete(event.id)
            is GenEvent_379_.Refresh -> loadAll()
            is GenEvent_379_.Search -> search(event.query)
            is GenEvent_379_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_379_.Loading; _state.value = GenState_379_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_379_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_379_.Success(searchUseCase(query)) } }
}
