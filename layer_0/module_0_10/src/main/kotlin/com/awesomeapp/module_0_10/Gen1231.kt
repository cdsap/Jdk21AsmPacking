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

data class GenModel_1231_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1231_ {
    data class Load(val id: Long) : GenEvent_1231_()
    data class Update(val model: GenModel_1231_) : GenEvent_1231_()
    data class Delete(val id: Long) : GenEvent_1231_()
    data object Refresh : GenEvent_1231_()
    data class Search(val query: String) : GenEvent_1231_()
    data class Filter(val predicate: String) : GenEvent_1231_()
}

sealed class GenState_1231_ {
    data object Idle : GenState_1231_()
    data object Loading : GenState_1231_()
    data class Success(val items: List<GenModel_1231_>) : GenState_1231_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1231_()
    data class Partial(val items: List<GenModel_1231_>, val hasMore: Boolean) : GenState_1231_()
}

interface GenRepository_1231_ {
    suspend fun getAll(): List<GenModel_1231_>
    suspend fun getById(id: Long): GenModel_1231_?
    suspend fun save(model: GenModel_1231_): GenModel_1231_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1231_>
}

@Singleton
class GenRepositoryImpl_1231_ @Inject constructor() : GenRepository_1231_ {
    private val store = mutableMapOf<Long, GenModel_1231_>()
    override suspend fun getAll(): List<GenModel_1231_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1231_? = store[id]
    override suspend fun save(model: GenModel_1231_): GenModel_1231_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1231_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1231_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1231_ @Inject constructor(
    private val repository: GenRepositoryImpl_1231_
) : GenUseCase_1231_<Unit, List<GenModel_1231_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1231_> = repository.getAll()
}

class GenSaveUseCase_1231_ @Inject constructor(
    private val repository: GenRepositoryImpl_1231_
) : GenUseCase_1231_<GenModel_1231_, GenModel_1231_> {
    override suspend fun invoke(params: GenModel_1231_): GenModel_1231_ = repository.save(params)
}

class GenDeleteUseCase_1231_ @Inject constructor(
    private val repository: GenRepositoryImpl_1231_
) : GenUseCase_1231_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1231_ @Inject constructor(
    private val repository: GenRepositoryImpl_1231_
) : GenUseCase_1231_<String, List<GenModel_1231_>> {
    override suspend fun invoke(params: String): List<GenModel_1231_> = repository.search(params)
}

abstract class GenMapper_1231_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1231_ : GenMapper_1231_<GenModel_1231_, String>() {
    override fun map(input: GenModel_1231_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1231_ : GenMapper_1231_<String, GenModel_1231_>() {
    override fun map(input: String): GenModel_1231_ {
        val parts = input.split(":")
        return GenModel_1231_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1231_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1231_,
    private val saveUseCase: GenSaveUseCase_1231_,
    private val deleteUseCase: GenDeleteUseCase_1231_,
    private val searchUseCase: GenSearchUseCase_1231_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1231_>(GenState_1231_.Idle)
    val state: StateFlow<GenState_1231_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1231_) {
        when (event) {
            is GenEvent_1231_.Load -> loadAll()
            is GenEvent_1231_.Update -> save(event.model)
            is GenEvent_1231_.Delete -> delete(event.id)
            is GenEvent_1231_.Refresh -> loadAll()
            is GenEvent_1231_.Search -> search(event.query)
            is GenEvent_1231_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1231_.Loading; _state.value = GenState_1231_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1231_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1231_.Success(searchUseCase(query)) } }
}
