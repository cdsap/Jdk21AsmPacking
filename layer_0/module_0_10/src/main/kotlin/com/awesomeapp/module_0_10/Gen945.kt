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

data class GenModel_945_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_945_ {
    data class Load(val id: Long) : GenEvent_945_()
    data class Update(val model: GenModel_945_) : GenEvent_945_()
    data class Delete(val id: Long) : GenEvent_945_()
    data object Refresh : GenEvent_945_()
    data class Search(val query: String) : GenEvent_945_()
    data class Filter(val predicate: String) : GenEvent_945_()
}

sealed class GenState_945_ {
    data object Idle : GenState_945_()
    data object Loading : GenState_945_()
    data class Success(val items: List<GenModel_945_>) : GenState_945_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_945_()
    data class Partial(val items: List<GenModel_945_>, val hasMore: Boolean) : GenState_945_()
}

interface GenRepository_945_ {
    suspend fun getAll(): List<GenModel_945_>
    suspend fun getById(id: Long): GenModel_945_?
    suspend fun save(model: GenModel_945_): GenModel_945_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_945_>
}

@Singleton
class GenRepositoryImpl_945_ @Inject constructor() : GenRepository_945_ {
    private val store = mutableMapOf<Long, GenModel_945_>()
    override suspend fun getAll(): List<GenModel_945_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_945_? = store[id]
    override suspend fun save(model: GenModel_945_): GenModel_945_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_945_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_945_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_945_ @Inject constructor(
    private val repository: GenRepositoryImpl_945_
) : GenUseCase_945_<Unit, List<GenModel_945_>> {
    override suspend fun invoke(params: Unit): List<GenModel_945_> = repository.getAll()
}

class GenSaveUseCase_945_ @Inject constructor(
    private val repository: GenRepositoryImpl_945_
) : GenUseCase_945_<GenModel_945_, GenModel_945_> {
    override suspend fun invoke(params: GenModel_945_): GenModel_945_ = repository.save(params)
}

class GenDeleteUseCase_945_ @Inject constructor(
    private val repository: GenRepositoryImpl_945_
) : GenUseCase_945_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_945_ @Inject constructor(
    private val repository: GenRepositoryImpl_945_
) : GenUseCase_945_<String, List<GenModel_945_>> {
    override suspend fun invoke(params: String): List<GenModel_945_> = repository.search(params)
}

abstract class GenMapper_945_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_945_ : GenMapper_945_<GenModel_945_, String>() {
    override fun map(input: GenModel_945_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_945_ : GenMapper_945_<String, GenModel_945_>() {
    override fun map(input: String): GenModel_945_ {
        val parts = input.split(":")
        return GenModel_945_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_945_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_945_,
    private val saveUseCase: GenSaveUseCase_945_,
    private val deleteUseCase: GenDeleteUseCase_945_,
    private val searchUseCase: GenSearchUseCase_945_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_945_>(GenState_945_.Idle)
    val state: StateFlow<GenState_945_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_945_) {
        when (event) {
            is GenEvent_945_.Load -> loadAll()
            is GenEvent_945_.Update -> save(event.model)
            is GenEvent_945_.Delete -> delete(event.id)
            is GenEvent_945_.Refresh -> loadAll()
            is GenEvent_945_.Search -> search(event.query)
            is GenEvent_945_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_945_.Loading; _state.value = GenState_945_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_945_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_945_.Success(searchUseCase(query)) } }
}
