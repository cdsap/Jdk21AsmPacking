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

data class GenModel_581_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_581_ {
    data class Load(val id: Long) : GenEvent_581_()
    data class Update(val model: GenModel_581_) : GenEvent_581_()
    data class Delete(val id: Long) : GenEvent_581_()
    data object Refresh : GenEvent_581_()
    data class Search(val query: String) : GenEvent_581_()
    data class Filter(val predicate: String) : GenEvent_581_()
}

sealed class GenState_581_ {
    data object Idle : GenState_581_()
    data object Loading : GenState_581_()
    data class Success(val items: List<GenModel_581_>) : GenState_581_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_581_()
    data class Partial(val items: List<GenModel_581_>, val hasMore: Boolean) : GenState_581_()
}

interface GenRepository_581_ {
    suspend fun getAll(): List<GenModel_581_>
    suspend fun getById(id: Long): GenModel_581_?
    suspend fun save(model: GenModel_581_): GenModel_581_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_581_>
}

@Singleton
class GenRepositoryImpl_581_ @Inject constructor() : GenRepository_581_ {
    private val store = mutableMapOf<Long, GenModel_581_>()
    override suspend fun getAll(): List<GenModel_581_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_581_? = store[id]
    override suspend fun save(model: GenModel_581_): GenModel_581_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_581_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_581_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_581_ @Inject constructor(
    private val repository: GenRepositoryImpl_581_
) : GenUseCase_581_<Unit, List<GenModel_581_>> {
    override suspend fun invoke(params: Unit): List<GenModel_581_> = repository.getAll()
}

class GenSaveUseCase_581_ @Inject constructor(
    private val repository: GenRepositoryImpl_581_
) : GenUseCase_581_<GenModel_581_, GenModel_581_> {
    override suspend fun invoke(params: GenModel_581_): GenModel_581_ = repository.save(params)
}

class GenDeleteUseCase_581_ @Inject constructor(
    private val repository: GenRepositoryImpl_581_
) : GenUseCase_581_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_581_ @Inject constructor(
    private val repository: GenRepositoryImpl_581_
) : GenUseCase_581_<String, List<GenModel_581_>> {
    override suspend fun invoke(params: String): List<GenModel_581_> = repository.search(params)
}

abstract class GenMapper_581_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_581_ : GenMapper_581_<GenModel_581_, String>() {
    override fun map(input: GenModel_581_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_581_ : GenMapper_581_<String, GenModel_581_>() {
    override fun map(input: String): GenModel_581_ {
        val parts = input.split(":")
        return GenModel_581_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_581_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_581_,
    private val saveUseCase: GenSaveUseCase_581_,
    private val deleteUseCase: GenDeleteUseCase_581_,
    private val searchUseCase: GenSearchUseCase_581_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_581_>(GenState_581_.Idle)
    val state: StateFlow<GenState_581_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_581_) {
        when (event) {
            is GenEvent_581_.Load -> loadAll()
            is GenEvent_581_.Update -> save(event.model)
            is GenEvent_581_.Delete -> delete(event.id)
            is GenEvent_581_.Refresh -> loadAll()
            is GenEvent_581_.Search -> search(event.query)
            is GenEvent_581_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_581_.Loading; _state.value = GenState_581_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_581_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_581_.Success(searchUseCase(query)) } }
}
