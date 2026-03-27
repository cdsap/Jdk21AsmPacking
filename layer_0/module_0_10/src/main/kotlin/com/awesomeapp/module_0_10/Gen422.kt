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

data class GenModel_422_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_422_ {
    data class Load(val id: Long) : GenEvent_422_()
    data class Update(val model: GenModel_422_) : GenEvent_422_()
    data class Delete(val id: Long) : GenEvent_422_()
    data object Refresh : GenEvent_422_()
    data class Search(val query: String) : GenEvent_422_()
    data class Filter(val predicate: String) : GenEvent_422_()
}

sealed class GenState_422_ {
    data object Idle : GenState_422_()
    data object Loading : GenState_422_()
    data class Success(val items: List<GenModel_422_>) : GenState_422_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_422_()
    data class Partial(val items: List<GenModel_422_>, val hasMore: Boolean) : GenState_422_()
}

interface GenRepository_422_ {
    suspend fun getAll(): List<GenModel_422_>
    suspend fun getById(id: Long): GenModel_422_?
    suspend fun save(model: GenModel_422_): GenModel_422_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_422_>
}

@Singleton
class GenRepositoryImpl_422_ @Inject constructor() : GenRepository_422_ {
    private val store = mutableMapOf<Long, GenModel_422_>()
    override suspend fun getAll(): List<GenModel_422_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_422_? = store[id]
    override suspend fun save(model: GenModel_422_): GenModel_422_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_422_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_422_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_422_ @Inject constructor(
    private val repository: GenRepositoryImpl_422_
) : GenUseCase_422_<Unit, List<GenModel_422_>> {
    override suspend fun invoke(params: Unit): List<GenModel_422_> = repository.getAll()
}

class GenSaveUseCase_422_ @Inject constructor(
    private val repository: GenRepositoryImpl_422_
) : GenUseCase_422_<GenModel_422_, GenModel_422_> {
    override suspend fun invoke(params: GenModel_422_): GenModel_422_ = repository.save(params)
}

class GenDeleteUseCase_422_ @Inject constructor(
    private val repository: GenRepositoryImpl_422_
) : GenUseCase_422_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_422_ @Inject constructor(
    private val repository: GenRepositoryImpl_422_
) : GenUseCase_422_<String, List<GenModel_422_>> {
    override suspend fun invoke(params: String): List<GenModel_422_> = repository.search(params)
}

abstract class GenMapper_422_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_422_ : GenMapper_422_<GenModel_422_, String>() {
    override fun map(input: GenModel_422_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_422_ : GenMapper_422_<String, GenModel_422_>() {
    override fun map(input: String): GenModel_422_ {
        val parts = input.split(":")
        return GenModel_422_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_422_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_422_,
    private val saveUseCase: GenSaveUseCase_422_,
    private val deleteUseCase: GenDeleteUseCase_422_,
    private val searchUseCase: GenSearchUseCase_422_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_422_>(GenState_422_.Idle)
    val state: StateFlow<GenState_422_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_422_) {
        when (event) {
            is GenEvent_422_.Load -> loadAll()
            is GenEvent_422_.Update -> save(event.model)
            is GenEvent_422_.Delete -> delete(event.id)
            is GenEvent_422_.Refresh -> loadAll()
            is GenEvent_422_.Search -> search(event.query)
            is GenEvent_422_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_422_.Loading; _state.value = GenState_422_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_422_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_422_.Success(searchUseCase(query)) } }
}
