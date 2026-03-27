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

data class GenModel_3323_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3323_ {
    data class Load(val id: Long) : GenEvent_3323_()
    data class Update(val model: GenModel_3323_) : GenEvent_3323_()
    data class Delete(val id: Long) : GenEvent_3323_()
    data object Refresh : GenEvent_3323_()
    data class Search(val query: String) : GenEvent_3323_()
    data class Filter(val predicate: String) : GenEvent_3323_()
}

sealed class GenState_3323_ {
    data object Idle : GenState_3323_()
    data object Loading : GenState_3323_()
    data class Success(val items: List<GenModel_3323_>) : GenState_3323_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3323_()
    data class Partial(val items: List<GenModel_3323_>, val hasMore: Boolean) : GenState_3323_()
}

interface GenRepository_3323_ {
    suspend fun getAll(): List<GenModel_3323_>
    suspend fun getById(id: Long): GenModel_3323_?
    suspend fun save(model: GenModel_3323_): GenModel_3323_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3323_>
}

@Singleton
class GenRepositoryImpl_3323_ @Inject constructor() : GenRepository_3323_ {
    private val store = mutableMapOf<Long, GenModel_3323_>()
    override suspend fun getAll(): List<GenModel_3323_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3323_? = store[id]
    override suspend fun save(model: GenModel_3323_): GenModel_3323_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3323_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3323_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3323_ @Inject constructor(
    private val repository: GenRepositoryImpl_3323_
) : GenUseCase_3323_<Unit, List<GenModel_3323_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3323_> = repository.getAll()
}

class GenSaveUseCase_3323_ @Inject constructor(
    private val repository: GenRepositoryImpl_3323_
) : GenUseCase_3323_<GenModel_3323_, GenModel_3323_> {
    override suspend fun invoke(params: GenModel_3323_): GenModel_3323_ = repository.save(params)
}

class GenDeleteUseCase_3323_ @Inject constructor(
    private val repository: GenRepositoryImpl_3323_
) : GenUseCase_3323_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3323_ @Inject constructor(
    private val repository: GenRepositoryImpl_3323_
) : GenUseCase_3323_<String, List<GenModel_3323_>> {
    override suspend fun invoke(params: String): List<GenModel_3323_> = repository.search(params)
}

abstract class GenMapper_3323_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3323_ : GenMapper_3323_<GenModel_3323_, String>() {
    override fun map(input: GenModel_3323_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3323_ : GenMapper_3323_<String, GenModel_3323_>() {
    override fun map(input: String): GenModel_3323_ {
        val parts = input.split(":")
        return GenModel_3323_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3323_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3323_,
    private val saveUseCase: GenSaveUseCase_3323_,
    private val deleteUseCase: GenDeleteUseCase_3323_,
    private val searchUseCase: GenSearchUseCase_3323_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3323_>(GenState_3323_.Idle)
    val state: StateFlow<GenState_3323_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3323_) {
        when (event) {
            is GenEvent_3323_.Load -> loadAll()
            is GenEvent_3323_.Update -> save(event.model)
            is GenEvent_3323_.Delete -> delete(event.id)
            is GenEvent_3323_.Refresh -> loadAll()
            is GenEvent_3323_.Search -> search(event.query)
            is GenEvent_3323_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3323_.Loading; _state.value = GenState_3323_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3323_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3323_.Success(searchUseCase(query)) } }
}
