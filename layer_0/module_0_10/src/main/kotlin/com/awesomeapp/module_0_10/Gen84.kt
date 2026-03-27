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

data class GenModel_84_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_84_ {
    data class Load(val id: Long) : GenEvent_84_()
    data class Update(val model: GenModel_84_) : GenEvent_84_()
    data class Delete(val id: Long) : GenEvent_84_()
    data object Refresh : GenEvent_84_()
    data class Search(val query: String) : GenEvent_84_()
    data class Filter(val predicate: String) : GenEvent_84_()
}

sealed class GenState_84_ {
    data object Idle : GenState_84_()
    data object Loading : GenState_84_()
    data class Success(val items: List<GenModel_84_>) : GenState_84_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_84_()
    data class Partial(val items: List<GenModel_84_>, val hasMore: Boolean) : GenState_84_()
}

interface GenRepository_84_ {
    suspend fun getAll(): List<GenModel_84_>
    suspend fun getById(id: Long): GenModel_84_?
    suspend fun save(model: GenModel_84_): GenModel_84_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_84_>
}

@Singleton
class GenRepositoryImpl_84_ @Inject constructor() : GenRepository_84_ {
    private val store = mutableMapOf<Long, GenModel_84_>()
    override suspend fun getAll(): List<GenModel_84_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_84_? = store[id]
    override suspend fun save(model: GenModel_84_): GenModel_84_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_84_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_84_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_84_ @Inject constructor(
    private val repository: GenRepositoryImpl_84_
) : GenUseCase_84_<Unit, List<GenModel_84_>> {
    override suspend fun invoke(params: Unit): List<GenModel_84_> = repository.getAll()
}

class GenSaveUseCase_84_ @Inject constructor(
    private val repository: GenRepositoryImpl_84_
) : GenUseCase_84_<GenModel_84_, GenModel_84_> {
    override suspend fun invoke(params: GenModel_84_): GenModel_84_ = repository.save(params)
}

class GenDeleteUseCase_84_ @Inject constructor(
    private val repository: GenRepositoryImpl_84_
) : GenUseCase_84_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_84_ @Inject constructor(
    private val repository: GenRepositoryImpl_84_
) : GenUseCase_84_<String, List<GenModel_84_>> {
    override suspend fun invoke(params: String): List<GenModel_84_> = repository.search(params)
}

abstract class GenMapper_84_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_84_ : GenMapper_84_<GenModel_84_, String>() {
    override fun map(input: GenModel_84_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_84_ : GenMapper_84_<String, GenModel_84_>() {
    override fun map(input: String): GenModel_84_ {
        val parts = input.split(":")
        return GenModel_84_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_84_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_84_,
    private val saveUseCase: GenSaveUseCase_84_,
    private val deleteUseCase: GenDeleteUseCase_84_,
    private val searchUseCase: GenSearchUseCase_84_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_84_>(GenState_84_.Idle)
    val state: StateFlow<GenState_84_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_84_) {
        when (event) {
            is GenEvent_84_.Load -> loadAll()
            is GenEvent_84_.Update -> save(event.model)
            is GenEvent_84_.Delete -> delete(event.id)
            is GenEvent_84_.Refresh -> loadAll()
            is GenEvent_84_.Search -> search(event.query)
            is GenEvent_84_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_84_.Loading; _state.value = GenState_84_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_84_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_84_.Success(searchUseCase(query)) } }
}
