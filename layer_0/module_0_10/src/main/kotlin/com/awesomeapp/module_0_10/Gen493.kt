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

data class GenModel_493_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_493_ {
    data class Load(val id: Long) : GenEvent_493_()
    data class Update(val model: GenModel_493_) : GenEvent_493_()
    data class Delete(val id: Long) : GenEvent_493_()
    data object Refresh : GenEvent_493_()
    data class Search(val query: String) : GenEvent_493_()
    data class Filter(val predicate: String) : GenEvent_493_()
}

sealed class GenState_493_ {
    data object Idle : GenState_493_()
    data object Loading : GenState_493_()
    data class Success(val items: List<GenModel_493_>) : GenState_493_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_493_()
    data class Partial(val items: List<GenModel_493_>, val hasMore: Boolean) : GenState_493_()
}

interface GenRepository_493_ {
    suspend fun getAll(): List<GenModel_493_>
    suspend fun getById(id: Long): GenModel_493_?
    suspend fun save(model: GenModel_493_): GenModel_493_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_493_>
}

@Singleton
class GenRepositoryImpl_493_ @Inject constructor() : GenRepository_493_ {
    private val store = mutableMapOf<Long, GenModel_493_>()
    override suspend fun getAll(): List<GenModel_493_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_493_? = store[id]
    override suspend fun save(model: GenModel_493_): GenModel_493_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_493_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_493_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_493_ @Inject constructor(
    private val repository: GenRepositoryImpl_493_
) : GenUseCase_493_<Unit, List<GenModel_493_>> {
    override suspend fun invoke(params: Unit): List<GenModel_493_> = repository.getAll()
}

class GenSaveUseCase_493_ @Inject constructor(
    private val repository: GenRepositoryImpl_493_
) : GenUseCase_493_<GenModel_493_, GenModel_493_> {
    override suspend fun invoke(params: GenModel_493_): GenModel_493_ = repository.save(params)
}

class GenDeleteUseCase_493_ @Inject constructor(
    private val repository: GenRepositoryImpl_493_
) : GenUseCase_493_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_493_ @Inject constructor(
    private val repository: GenRepositoryImpl_493_
) : GenUseCase_493_<String, List<GenModel_493_>> {
    override suspend fun invoke(params: String): List<GenModel_493_> = repository.search(params)
}

abstract class GenMapper_493_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_493_ : GenMapper_493_<GenModel_493_, String>() {
    override fun map(input: GenModel_493_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_493_ : GenMapper_493_<String, GenModel_493_>() {
    override fun map(input: String): GenModel_493_ {
        val parts = input.split(":")
        return GenModel_493_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_493_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_493_,
    private val saveUseCase: GenSaveUseCase_493_,
    private val deleteUseCase: GenDeleteUseCase_493_,
    private val searchUseCase: GenSearchUseCase_493_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_493_>(GenState_493_.Idle)
    val state: StateFlow<GenState_493_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_493_) {
        when (event) {
            is GenEvent_493_.Load -> loadAll()
            is GenEvent_493_.Update -> save(event.model)
            is GenEvent_493_.Delete -> delete(event.id)
            is GenEvent_493_.Refresh -> loadAll()
            is GenEvent_493_.Search -> search(event.query)
            is GenEvent_493_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_493_.Loading; _state.value = GenState_493_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_493_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_493_.Success(searchUseCase(query)) } }
}
