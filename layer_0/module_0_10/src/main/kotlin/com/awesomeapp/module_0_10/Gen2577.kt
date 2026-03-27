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

data class GenModel_2577_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2577_ {
    data class Load(val id: Long) : GenEvent_2577_()
    data class Update(val model: GenModel_2577_) : GenEvent_2577_()
    data class Delete(val id: Long) : GenEvent_2577_()
    data object Refresh : GenEvent_2577_()
    data class Search(val query: String) : GenEvent_2577_()
    data class Filter(val predicate: String) : GenEvent_2577_()
}

sealed class GenState_2577_ {
    data object Idle : GenState_2577_()
    data object Loading : GenState_2577_()
    data class Success(val items: List<GenModel_2577_>) : GenState_2577_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2577_()
    data class Partial(val items: List<GenModel_2577_>, val hasMore: Boolean) : GenState_2577_()
}

interface GenRepository_2577_ {
    suspend fun getAll(): List<GenModel_2577_>
    suspend fun getById(id: Long): GenModel_2577_?
    suspend fun save(model: GenModel_2577_): GenModel_2577_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2577_>
}

@Singleton
class GenRepositoryImpl_2577_ @Inject constructor() : GenRepository_2577_ {
    private val store = mutableMapOf<Long, GenModel_2577_>()
    override suspend fun getAll(): List<GenModel_2577_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2577_? = store[id]
    override suspend fun save(model: GenModel_2577_): GenModel_2577_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2577_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2577_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2577_ @Inject constructor(
    private val repository: GenRepositoryImpl_2577_
) : GenUseCase_2577_<Unit, List<GenModel_2577_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2577_> = repository.getAll()
}

class GenSaveUseCase_2577_ @Inject constructor(
    private val repository: GenRepositoryImpl_2577_
) : GenUseCase_2577_<GenModel_2577_, GenModel_2577_> {
    override suspend fun invoke(params: GenModel_2577_): GenModel_2577_ = repository.save(params)
}

class GenDeleteUseCase_2577_ @Inject constructor(
    private val repository: GenRepositoryImpl_2577_
) : GenUseCase_2577_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2577_ @Inject constructor(
    private val repository: GenRepositoryImpl_2577_
) : GenUseCase_2577_<String, List<GenModel_2577_>> {
    override suspend fun invoke(params: String): List<GenModel_2577_> = repository.search(params)
}

abstract class GenMapper_2577_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2577_ : GenMapper_2577_<GenModel_2577_, String>() {
    override fun map(input: GenModel_2577_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2577_ : GenMapper_2577_<String, GenModel_2577_>() {
    override fun map(input: String): GenModel_2577_ {
        val parts = input.split(":")
        return GenModel_2577_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2577_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2577_,
    private val saveUseCase: GenSaveUseCase_2577_,
    private val deleteUseCase: GenDeleteUseCase_2577_,
    private val searchUseCase: GenSearchUseCase_2577_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2577_>(GenState_2577_.Idle)
    val state: StateFlow<GenState_2577_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2577_) {
        when (event) {
            is GenEvent_2577_.Load -> loadAll()
            is GenEvent_2577_.Update -> save(event.model)
            is GenEvent_2577_.Delete -> delete(event.id)
            is GenEvent_2577_.Refresh -> loadAll()
            is GenEvent_2577_.Search -> search(event.query)
            is GenEvent_2577_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2577_.Loading; _state.value = GenState_2577_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2577_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2577_.Success(searchUseCase(query)) } }
}
