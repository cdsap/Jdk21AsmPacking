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

data class GenModel_231_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_231_ {
    data class Load(val id: Long) : GenEvent_231_()
    data class Update(val model: GenModel_231_) : GenEvent_231_()
    data class Delete(val id: Long) : GenEvent_231_()
    data object Refresh : GenEvent_231_()
    data class Search(val query: String) : GenEvent_231_()
    data class Filter(val predicate: String) : GenEvent_231_()
}

sealed class GenState_231_ {
    data object Idle : GenState_231_()
    data object Loading : GenState_231_()
    data class Success(val items: List<GenModel_231_>) : GenState_231_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_231_()
    data class Partial(val items: List<GenModel_231_>, val hasMore: Boolean) : GenState_231_()
}

interface GenRepository_231_ {
    suspend fun getAll(): List<GenModel_231_>
    suspend fun getById(id: Long): GenModel_231_?
    suspend fun save(model: GenModel_231_): GenModel_231_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_231_>
}

@Singleton
class GenRepositoryImpl_231_ @Inject constructor() : GenRepository_231_ {
    private val store = mutableMapOf<Long, GenModel_231_>()
    override suspend fun getAll(): List<GenModel_231_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_231_? = store[id]
    override suspend fun save(model: GenModel_231_): GenModel_231_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_231_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_231_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_231_ @Inject constructor(
    private val repository: GenRepositoryImpl_231_
) : GenUseCase_231_<Unit, List<GenModel_231_>> {
    override suspend fun invoke(params: Unit): List<GenModel_231_> = repository.getAll()
}

class GenSaveUseCase_231_ @Inject constructor(
    private val repository: GenRepositoryImpl_231_
) : GenUseCase_231_<GenModel_231_, GenModel_231_> {
    override suspend fun invoke(params: GenModel_231_): GenModel_231_ = repository.save(params)
}

class GenDeleteUseCase_231_ @Inject constructor(
    private val repository: GenRepositoryImpl_231_
) : GenUseCase_231_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_231_ @Inject constructor(
    private val repository: GenRepositoryImpl_231_
) : GenUseCase_231_<String, List<GenModel_231_>> {
    override suspend fun invoke(params: String): List<GenModel_231_> = repository.search(params)
}

abstract class GenMapper_231_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_231_ : GenMapper_231_<GenModel_231_, String>() {
    override fun map(input: GenModel_231_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_231_ : GenMapper_231_<String, GenModel_231_>() {
    override fun map(input: String): GenModel_231_ {
        val parts = input.split(":")
        return GenModel_231_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_231_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_231_,
    private val saveUseCase: GenSaveUseCase_231_,
    private val deleteUseCase: GenDeleteUseCase_231_,
    private val searchUseCase: GenSearchUseCase_231_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_231_>(GenState_231_.Idle)
    val state: StateFlow<GenState_231_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_231_) {
        when (event) {
            is GenEvent_231_.Load -> loadAll()
            is GenEvent_231_.Update -> save(event.model)
            is GenEvent_231_.Delete -> delete(event.id)
            is GenEvent_231_.Refresh -> loadAll()
            is GenEvent_231_.Search -> search(event.query)
            is GenEvent_231_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_231_.Loading; _state.value = GenState_231_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_231_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_231_.Success(searchUseCase(query)) } }
}
