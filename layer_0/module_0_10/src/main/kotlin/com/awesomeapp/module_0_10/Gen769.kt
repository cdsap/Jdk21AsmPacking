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

data class GenModel_769_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_769_ {
    data class Load(val id: Long) : GenEvent_769_()
    data class Update(val model: GenModel_769_) : GenEvent_769_()
    data class Delete(val id: Long) : GenEvent_769_()
    data object Refresh : GenEvent_769_()
    data class Search(val query: String) : GenEvent_769_()
    data class Filter(val predicate: String) : GenEvent_769_()
}

sealed class GenState_769_ {
    data object Idle : GenState_769_()
    data object Loading : GenState_769_()
    data class Success(val items: List<GenModel_769_>) : GenState_769_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_769_()
    data class Partial(val items: List<GenModel_769_>, val hasMore: Boolean) : GenState_769_()
}

interface GenRepository_769_ {
    suspend fun getAll(): List<GenModel_769_>
    suspend fun getById(id: Long): GenModel_769_?
    suspend fun save(model: GenModel_769_): GenModel_769_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_769_>
}

@Singleton
class GenRepositoryImpl_769_ @Inject constructor() : GenRepository_769_ {
    private val store = mutableMapOf<Long, GenModel_769_>()
    override suspend fun getAll(): List<GenModel_769_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_769_? = store[id]
    override suspend fun save(model: GenModel_769_): GenModel_769_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_769_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_769_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_769_ @Inject constructor(
    private val repository: GenRepositoryImpl_769_
) : GenUseCase_769_<Unit, List<GenModel_769_>> {
    override suspend fun invoke(params: Unit): List<GenModel_769_> = repository.getAll()
}

class GenSaveUseCase_769_ @Inject constructor(
    private val repository: GenRepositoryImpl_769_
) : GenUseCase_769_<GenModel_769_, GenModel_769_> {
    override suspend fun invoke(params: GenModel_769_): GenModel_769_ = repository.save(params)
}

class GenDeleteUseCase_769_ @Inject constructor(
    private val repository: GenRepositoryImpl_769_
) : GenUseCase_769_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_769_ @Inject constructor(
    private val repository: GenRepositoryImpl_769_
) : GenUseCase_769_<String, List<GenModel_769_>> {
    override suspend fun invoke(params: String): List<GenModel_769_> = repository.search(params)
}

abstract class GenMapper_769_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_769_ : GenMapper_769_<GenModel_769_, String>() {
    override fun map(input: GenModel_769_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_769_ : GenMapper_769_<String, GenModel_769_>() {
    override fun map(input: String): GenModel_769_ {
        val parts = input.split(":")
        return GenModel_769_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_769_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_769_,
    private val saveUseCase: GenSaveUseCase_769_,
    private val deleteUseCase: GenDeleteUseCase_769_,
    private val searchUseCase: GenSearchUseCase_769_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_769_>(GenState_769_.Idle)
    val state: StateFlow<GenState_769_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_769_) {
        when (event) {
            is GenEvent_769_.Load -> loadAll()
            is GenEvent_769_.Update -> save(event.model)
            is GenEvent_769_.Delete -> delete(event.id)
            is GenEvent_769_.Refresh -> loadAll()
            is GenEvent_769_.Search -> search(event.query)
            is GenEvent_769_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_769_.Loading; _state.value = GenState_769_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_769_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_769_.Success(searchUseCase(query)) } }
}
