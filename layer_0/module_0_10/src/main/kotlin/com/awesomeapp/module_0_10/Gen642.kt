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

data class GenModel_642_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_642_ {
    data class Load(val id: Long) : GenEvent_642_()
    data class Update(val model: GenModel_642_) : GenEvent_642_()
    data class Delete(val id: Long) : GenEvent_642_()
    data object Refresh : GenEvent_642_()
    data class Search(val query: String) : GenEvent_642_()
    data class Filter(val predicate: String) : GenEvent_642_()
}

sealed class GenState_642_ {
    data object Idle : GenState_642_()
    data object Loading : GenState_642_()
    data class Success(val items: List<GenModel_642_>) : GenState_642_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_642_()
    data class Partial(val items: List<GenModel_642_>, val hasMore: Boolean) : GenState_642_()
}

interface GenRepository_642_ {
    suspend fun getAll(): List<GenModel_642_>
    suspend fun getById(id: Long): GenModel_642_?
    suspend fun save(model: GenModel_642_): GenModel_642_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_642_>
}

@Singleton
class GenRepositoryImpl_642_ @Inject constructor() : GenRepository_642_ {
    private val store = mutableMapOf<Long, GenModel_642_>()
    override suspend fun getAll(): List<GenModel_642_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_642_? = store[id]
    override suspend fun save(model: GenModel_642_): GenModel_642_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_642_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_642_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_642_ @Inject constructor(
    private val repository: GenRepositoryImpl_642_
) : GenUseCase_642_<Unit, List<GenModel_642_>> {
    override suspend fun invoke(params: Unit): List<GenModel_642_> = repository.getAll()
}

class GenSaveUseCase_642_ @Inject constructor(
    private val repository: GenRepositoryImpl_642_
) : GenUseCase_642_<GenModel_642_, GenModel_642_> {
    override suspend fun invoke(params: GenModel_642_): GenModel_642_ = repository.save(params)
}

class GenDeleteUseCase_642_ @Inject constructor(
    private val repository: GenRepositoryImpl_642_
) : GenUseCase_642_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_642_ @Inject constructor(
    private val repository: GenRepositoryImpl_642_
) : GenUseCase_642_<String, List<GenModel_642_>> {
    override suspend fun invoke(params: String): List<GenModel_642_> = repository.search(params)
}

abstract class GenMapper_642_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_642_ : GenMapper_642_<GenModel_642_, String>() {
    override fun map(input: GenModel_642_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_642_ : GenMapper_642_<String, GenModel_642_>() {
    override fun map(input: String): GenModel_642_ {
        val parts = input.split(":")
        return GenModel_642_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_642_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_642_,
    private val saveUseCase: GenSaveUseCase_642_,
    private val deleteUseCase: GenDeleteUseCase_642_,
    private val searchUseCase: GenSearchUseCase_642_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_642_>(GenState_642_.Idle)
    val state: StateFlow<GenState_642_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_642_) {
        when (event) {
            is GenEvent_642_.Load -> loadAll()
            is GenEvent_642_.Update -> save(event.model)
            is GenEvent_642_.Delete -> delete(event.id)
            is GenEvent_642_.Refresh -> loadAll()
            is GenEvent_642_.Search -> search(event.query)
            is GenEvent_642_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_642_.Loading; _state.value = GenState_642_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_642_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_642_.Success(searchUseCase(query)) } }
}
