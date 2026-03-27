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

data class GenModel_445_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_445_ {
    data class Load(val id: Long) : GenEvent_445_()
    data class Update(val model: GenModel_445_) : GenEvent_445_()
    data class Delete(val id: Long) : GenEvent_445_()
    data object Refresh : GenEvent_445_()
    data class Search(val query: String) : GenEvent_445_()
    data class Filter(val predicate: String) : GenEvent_445_()
}

sealed class GenState_445_ {
    data object Idle : GenState_445_()
    data object Loading : GenState_445_()
    data class Success(val items: List<GenModel_445_>) : GenState_445_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_445_()
    data class Partial(val items: List<GenModel_445_>, val hasMore: Boolean) : GenState_445_()
}

interface GenRepository_445_ {
    suspend fun getAll(): List<GenModel_445_>
    suspend fun getById(id: Long): GenModel_445_?
    suspend fun save(model: GenModel_445_): GenModel_445_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_445_>
}

@Singleton
class GenRepositoryImpl_445_ @Inject constructor() : GenRepository_445_ {
    private val store = mutableMapOf<Long, GenModel_445_>()
    override suspend fun getAll(): List<GenModel_445_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_445_? = store[id]
    override suspend fun save(model: GenModel_445_): GenModel_445_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_445_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_445_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_445_ @Inject constructor(
    private val repository: GenRepositoryImpl_445_
) : GenUseCase_445_<Unit, List<GenModel_445_>> {
    override suspend fun invoke(params: Unit): List<GenModel_445_> = repository.getAll()
}

class GenSaveUseCase_445_ @Inject constructor(
    private val repository: GenRepositoryImpl_445_
) : GenUseCase_445_<GenModel_445_, GenModel_445_> {
    override suspend fun invoke(params: GenModel_445_): GenModel_445_ = repository.save(params)
}

class GenDeleteUseCase_445_ @Inject constructor(
    private val repository: GenRepositoryImpl_445_
) : GenUseCase_445_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_445_ @Inject constructor(
    private val repository: GenRepositoryImpl_445_
) : GenUseCase_445_<String, List<GenModel_445_>> {
    override suspend fun invoke(params: String): List<GenModel_445_> = repository.search(params)
}

abstract class GenMapper_445_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_445_ : GenMapper_445_<GenModel_445_, String>() {
    override fun map(input: GenModel_445_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_445_ : GenMapper_445_<String, GenModel_445_>() {
    override fun map(input: String): GenModel_445_ {
        val parts = input.split(":")
        return GenModel_445_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_445_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_445_,
    private val saveUseCase: GenSaveUseCase_445_,
    private val deleteUseCase: GenDeleteUseCase_445_,
    private val searchUseCase: GenSearchUseCase_445_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_445_>(GenState_445_.Idle)
    val state: StateFlow<GenState_445_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_445_) {
        when (event) {
            is GenEvent_445_.Load -> loadAll()
            is GenEvent_445_.Update -> save(event.model)
            is GenEvent_445_.Delete -> delete(event.id)
            is GenEvent_445_.Refresh -> loadAll()
            is GenEvent_445_.Search -> search(event.query)
            is GenEvent_445_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_445_.Loading; _state.value = GenState_445_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_445_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_445_.Success(searchUseCase(query)) } }
}
