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

data class GenModel_755_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_755_ {
    data class Load(val id: Long) : GenEvent_755_()
    data class Update(val model: GenModel_755_) : GenEvent_755_()
    data class Delete(val id: Long) : GenEvent_755_()
    data object Refresh : GenEvent_755_()
    data class Search(val query: String) : GenEvent_755_()
    data class Filter(val predicate: String) : GenEvent_755_()
}

sealed class GenState_755_ {
    data object Idle : GenState_755_()
    data object Loading : GenState_755_()
    data class Success(val items: List<GenModel_755_>) : GenState_755_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_755_()
    data class Partial(val items: List<GenModel_755_>, val hasMore: Boolean) : GenState_755_()
}

interface GenRepository_755_ {
    suspend fun getAll(): List<GenModel_755_>
    suspend fun getById(id: Long): GenModel_755_?
    suspend fun save(model: GenModel_755_): GenModel_755_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_755_>
}

@Singleton
class GenRepositoryImpl_755_ @Inject constructor() : GenRepository_755_ {
    private val store = mutableMapOf<Long, GenModel_755_>()
    override suspend fun getAll(): List<GenModel_755_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_755_? = store[id]
    override suspend fun save(model: GenModel_755_): GenModel_755_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_755_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_755_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_755_ @Inject constructor(
    private val repository: GenRepositoryImpl_755_
) : GenUseCase_755_<Unit, List<GenModel_755_>> {
    override suspend fun invoke(params: Unit): List<GenModel_755_> = repository.getAll()
}

class GenSaveUseCase_755_ @Inject constructor(
    private val repository: GenRepositoryImpl_755_
) : GenUseCase_755_<GenModel_755_, GenModel_755_> {
    override suspend fun invoke(params: GenModel_755_): GenModel_755_ = repository.save(params)
}

class GenDeleteUseCase_755_ @Inject constructor(
    private val repository: GenRepositoryImpl_755_
) : GenUseCase_755_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_755_ @Inject constructor(
    private val repository: GenRepositoryImpl_755_
) : GenUseCase_755_<String, List<GenModel_755_>> {
    override suspend fun invoke(params: String): List<GenModel_755_> = repository.search(params)
}

abstract class GenMapper_755_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_755_ : GenMapper_755_<GenModel_755_, String>() {
    override fun map(input: GenModel_755_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_755_ : GenMapper_755_<String, GenModel_755_>() {
    override fun map(input: String): GenModel_755_ {
        val parts = input.split(":")
        return GenModel_755_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_755_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_755_,
    private val saveUseCase: GenSaveUseCase_755_,
    private val deleteUseCase: GenDeleteUseCase_755_,
    private val searchUseCase: GenSearchUseCase_755_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_755_>(GenState_755_.Idle)
    val state: StateFlow<GenState_755_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_755_) {
        when (event) {
            is GenEvent_755_.Load -> loadAll()
            is GenEvent_755_.Update -> save(event.model)
            is GenEvent_755_.Delete -> delete(event.id)
            is GenEvent_755_.Refresh -> loadAll()
            is GenEvent_755_.Search -> search(event.query)
            is GenEvent_755_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_755_.Loading; _state.value = GenState_755_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_755_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_755_.Success(searchUseCase(query)) } }
}
