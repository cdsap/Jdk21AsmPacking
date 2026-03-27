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

data class GenModel_71_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_71_ {
    data class Load(val id: Long) : GenEvent_71_()
    data class Update(val model: GenModel_71_) : GenEvent_71_()
    data class Delete(val id: Long) : GenEvent_71_()
    data object Refresh : GenEvent_71_()
    data class Search(val query: String) : GenEvent_71_()
    data class Filter(val predicate: String) : GenEvent_71_()
}

sealed class GenState_71_ {
    data object Idle : GenState_71_()
    data object Loading : GenState_71_()
    data class Success(val items: List<GenModel_71_>) : GenState_71_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_71_()
    data class Partial(val items: List<GenModel_71_>, val hasMore: Boolean) : GenState_71_()
}

interface GenRepository_71_ {
    suspend fun getAll(): List<GenModel_71_>
    suspend fun getById(id: Long): GenModel_71_?
    suspend fun save(model: GenModel_71_): GenModel_71_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_71_>
}

@Singleton
class GenRepositoryImpl_71_ @Inject constructor() : GenRepository_71_ {
    private val store = mutableMapOf<Long, GenModel_71_>()
    override suspend fun getAll(): List<GenModel_71_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_71_? = store[id]
    override suspend fun save(model: GenModel_71_): GenModel_71_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_71_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_71_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_71_ @Inject constructor(
    private val repository: GenRepositoryImpl_71_
) : GenUseCase_71_<Unit, List<GenModel_71_>> {
    override suspend fun invoke(params: Unit): List<GenModel_71_> = repository.getAll()
}

class GenSaveUseCase_71_ @Inject constructor(
    private val repository: GenRepositoryImpl_71_
) : GenUseCase_71_<GenModel_71_, GenModel_71_> {
    override suspend fun invoke(params: GenModel_71_): GenModel_71_ = repository.save(params)
}

class GenDeleteUseCase_71_ @Inject constructor(
    private val repository: GenRepositoryImpl_71_
) : GenUseCase_71_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_71_ @Inject constructor(
    private val repository: GenRepositoryImpl_71_
) : GenUseCase_71_<String, List<GenModel_71_>> {
    override suspend fun invoke(params: String): List<GenModel_71_> = repository.search(params)
}

abstract class GenMapper_71_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_71_ : GenMapper_71_<GenModel_71_, String>() {
    override fun map(input: GenModel_71_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_71_ : GenMapper_71_<String, GenModel_71_>() {
    override fun map(input: String): GenModel_71_ {
        val parts = input.split(":")
        return GenModel_71_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_71_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_71_,
    private val saveUseCase: GenSaveUseCase_71_,
    private val deleteUseCase: GenDeleteUseCase_71_,
    private val searchUseCase: GenSearchUseCase_71_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_71_>(GenState_71_.Idle)
    val state: StateFlow<GenState_71_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_71_) {
        when (event) {
            is GenEvent_71_.Load -> loadAll()
            is GenEvent_71_.Update -> save(event.model)
            is GenEvent_71_.Delete -> delete(event.id)
            is GenEvent_71_.Refresh -> loadAll()
            is GenEvent_71_.Search -> search(event.query)
            is GenEvent_71_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_71_.Loading; _state.value = GenState_71_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_71_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_71_.Success(searchUseCase(query)) } }
}
