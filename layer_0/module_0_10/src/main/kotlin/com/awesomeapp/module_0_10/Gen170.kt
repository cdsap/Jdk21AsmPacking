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

data class GenModel_170_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_170_ {
    data class Load(val id: Long) : GenEvent_170_()
    data class Update(val model: GenModel_170_) : GenEvent_170_()
    data class Delete(val id: Long) : GenEvent_170_()
    data object Refresh : GenEvent_170_()
    data class Search(val query: String) : GenEvent_170_()
    data class Filter(val predicate: String) : GenEvent_170_()
}

sealed class GenState_170_ {
    data object Idle : GenState_170_()
    data object Loading : GenState_170_()
    data class Success(val items: List<GenModel_170_>) : GenState_170_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_170_()
    data class Partial(val items: List<GenModel_170_>, val hasMore: Boolean) : GenState_170_()
}

interface GenRepository_170_ {
    suspend fun getAll(): List<GenModel_170_>
    suspend fun getById(id: Long): GenModel_170_?
    suspend fun save(model: GenModel_170_): GenModel_170_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_170_>
}

@Singleton
class GenRepositoryImpl_170_ @Inject constructor() : GenRepository_170_ {
    private val store = mutableMapOf<Long, GenModel_170_>()
    override suspend fun getAll(): List<GenModel_170_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_170_? = store[id]
    override suspend fun save(model: GenModel_170_): GenModel_170_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_170_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_170_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_170_ @Inject constructor(
    private val repository: GenRepositoryImpl_170_
) : GenUseCase_170_<Unit, List<GenModel_170_>> {
    override suspend fun invoke(params: Unit): List<GenModel_170_> = repository.getAll()
}

class GenSaveUseCase_170_ @Inject constructor(
    private val repository: GenRepositoryImpl_170_
) : GenUseCase_170_<GenModel_170_, GenModel_170_> {
    override suspend fun invoke(params: GenModel_170_): GenModel_170_ = repository.save(params)
}

class GenDeleteUseCase_170_ @Inject constructor(
    private val repository: GenRepositoryImpl_170_
) : GenUseCase_170_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_170_ @Inject constructor(
    private val repository: GenRepositoryImpl_170_
) : GenUseCase_170_<String, List<GenModel_170_>> {
    override suspend fun invoke(params: String): List<GenModel_170_> = repository.search(params)
}

abstract class GenMapper_170_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_170_ : GenMapper_170_<GenModel_170_, String>() {
    override fun map(input: GenModel_170_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_170_ : GenMapper_170_<String, GenModel_170_>() {
    override fun map(input: String): GenModel_170_ {
        val parts = input.split(":")
        return GenModel_170_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_170_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_170_,
    private val saveUseCase: GenSaveUseCase_170_,
    private val deleteUseCase: GenDeleteUseCase_170_,
    private val searchUseCase: GenSearchUseCase_170_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_170_>(GenState_170_.Idle)
    val state: StateFlow<GenState_170_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_170_) {
        when (event) {
            is GenEvent_170_.Load -> loadAll()
            is GenEvent_170_.Update -> save(event.model)
            is GenEvent_170_.Delete -> delete(event.id)
            is GenEvent_170_.Refresh -> loadAll()
            is GenEvent_170_.Search -> search(event.query)
            is GenEvent_170_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_170_.Loading; _state.value = GenState_170_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_170_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_170_.Success(searchUseCase(query)) } }
}
