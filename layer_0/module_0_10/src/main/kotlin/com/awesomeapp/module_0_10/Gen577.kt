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

data class GenModel_577_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_577_ {
    data class Load(val id: Long) : GenEvent_577_()
    data class Update(val model: GenModel_577_) : GenEvent_577_()
    data class Delete(val id: Long) : GenEvent_577_()
    data object Refresh : GenEvent_577_()
    data class Search(val query: String) : GenEvent_577_()
    data class Filter(val predicate: String) : GenEvent_577_()
}

sealed class GenState_577_ {
    data object Idle : GenState_577_()
    data object Loading : GenState_577_()
    data class Success(val items: List<GenModel_577_>) : GenState_577_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_577_()
    data class Partial(val items: List<GenModel_577_>, val hasMore: Boolean) : GenState_577_()
}

interface GenRepository_577_ {
    suspend fun getAll(): List<GenModel_577_>
    suspend fun getById(id: Long): GenModel_577_?
    suspend fun save(model: GenModel_577_): GenModel_577_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_577_>
}

@Singleton
class GenRepositoryImpl_577_ @Inject constructor() : GenRepository_577_ {
    private val store = mutableMapOf<Long, GenModel_577_>()
    override suspend fun getAll(): List<GenModel_577_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_577_? = store[id]
    override suspend fun save(model: GenModel_577_): GenModel_577_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_577_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_577_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_577_ @Inject constructor(
    private val repository: GenRepositoryImpl_577_
) : GenUseCase_577_<Unit, List<GenModel_577_>> {
    override suspend fun invoke(params: Unit): List<GenModel_577_> = repository.getAll()
}

class GenSaveUseCase_577_ @Inject constructor(
    private val repository: GenRepositoryImpl_577_
) : GenUseCase_577_<GenModel_577_, GenModel_577_> {
    override suspend fun invoke(params: GenModel_577_): GenModel_577_ = repository.save(params)
}

class GenDeleteUseCase_577_ @Inject constructor(
    private val repository: GenRepositoryImpl_577_
) : GenUseCase_577_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_577_ @Inject constructor(
    private val repository: GenRepositoryImpl_577_
) : GenUseCase_577_<String, List<GenModel_577_>> {
    override suspend fun invoke(params: String): List<GenModel_577_> = repository.search(params)
}

abstract class GenMapper_577_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_577_ : GenMapper_577_<GenModel_577_, String>() {
    override fun map(input: GenModel_577_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_577_ : GenMapper_577_<String, GenModel_577_>() {
    override fun map(input: String): GenModel_577_ {
        val parts = input.split(":")
        return GenModel_577_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_577_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_577_,
    private val saveUseCase: GenSaveUseCase_577_,
    private val deleteUseCase: GenDeleteUseCase_577_,
    private val searchUseCase: GenSearchUseCase_577_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_577_>(GenState_577_.Idle)
    val state: StateFlow<GenState_577_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_577_) {
        when (event) {
            is GenEvent_577_.Load -> loadAll()
            is GenEvent_577_.Update -> save(event.model)
            is GenEvent_577_.Delete -> delete(event.id)
            is GenEvent_577_.Refresh -> loadAll()
            is GenEvent_577_.Search -> search(event.query)
            is GenEvent_577_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_577_.Loading; _state.value = GenState_577_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_577_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_577_.Success(searchUseCase(query)) } }
}
