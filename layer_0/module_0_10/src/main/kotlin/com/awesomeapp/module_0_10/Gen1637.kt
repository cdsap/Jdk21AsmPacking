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

data class GenModel_1637_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1637_ {
    data class Load(val id: Long) : GenEvent_1637_()
    data class Update(val model: GenModel_1637_) : GenEvent_1637_()
    data class Delete(val id: Long) : GenEvent_1637_()
    data object Refresh : GenEvent_1637_()
    data class Search(val query: String) : GenEvent_1637_()
    data class Filter(val predicate: String) : GenEvent_1637_()
}

sealed class GenState_1637_ {
    data object Idle : GenState_1637_()
    data object Loading : GenState_1637_()
    data class Success(val items: List<GenModel_1637_>) : GenState_1637_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1637_()
    data class Partial(val items: List<GenModel_1637_>, val hasMore: Boolean) : GenState_1637_()
}

interface GenRepository_1637_ {
    suspend fun getAll(): List<GenModel_1637_>
    suspend fun getById(id: Long): GenModel_1637_?
    suspend fun save(model: GenModel_1637_): GenModel_1637_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1637_>
}

@Singleton
class GenRepositoryImpl_1637_ @Inject constructor() : GenRepository_1637_ {
    private val store = mutableMapOf<Long, GenModel_1637_>()
    override suspend fun getAll(): List<GenModel_1637_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1637_? = store[id]
    override suspend fun save(model: GenModel_1637_): GenModel_1637_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1637_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1637_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1637_ @Inject constructor(
    private val repository: GenRepositoryImpl_1637_
) : GenUseCase_1637_<Unit, List<GenModel_1637_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1637_> = repository.getAll()
}

class GenSaveUseCase_1637_ @Inject constructor(
    private val repository: GenRepositoryImpl_1637_
) : GenUseCase_1637_<GenModel_1637_, GenModel_1637_> {
    override suspend fun invoke(params: GenModel_1637_): GenModel_1637_ = repository.save(params)
}

class GenDeleteUseCase_1637_ @Inject constructor(
    private val repository: GenRepositoryImpl_1637_
) : GenUseCase_1637_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1637_ @Inject constructor(
    private val repository: GenRepositoryImpl_1637_
) : GenUseCase_1637_<String, List<GenModel_1637_>> {
    override suspend fun invoke(params: String): List<GenModel_1637_> = repository.search(params)
}

abstract class GenMapper_1637_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1637_ : GenMapper_1637_<GenModel_1637_, String>() {
    override fun map(input: GenModel_1637_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1637_ : GenMapper_1637_<String, GenModel_1637_>() {
    override fun map(input: String): GenModel_1637_ {
        val parts = input.split(":")
        return GenModel_1637_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1637_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1637_,
    private val saveUseCase: GenSaveUseCase_1637_,
    private val deleteUseCase: GenDeleteUseCase_1637_,
    private val searchUseCase: GenSearchUseCase_1637_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1637_>(GenState_1637_.Idle)
    val state: StateFlow<GenState_1637_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1637_) {
        when (event) {
            is GenEvent_1637_.Load -> loadAll()
            is GenEvent_1637_.Update -> save(event.model)
            is GenEvent_1637_.Delete -> delete(event.id)
            is GenEvent_1637_.Refresh -> loadAll()
            is GenEvent_1637_.Search -> search(event.query)
            is GenEvent_1637_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1637_.Loading; _state.value = GenState_1637_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1637_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1637_.Success(searchUseCase(query)) } }
}
