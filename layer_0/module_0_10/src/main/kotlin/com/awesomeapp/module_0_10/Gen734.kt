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

data class GenModel_734_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_734_ {
    data class Load(val id: Long) : GenEvent_734_()
    data class Update(val model: GenModel_734_) : GenEvent_734_()
    data class Delete(val id: Long) : GenEvent_734_()
    data object Refresh : GenEvent_734_()
    data class Search(val query: String) : GenEvent_734_()
    data class Filter(val predicate: String) : GenEvent_734_()
}

sealed class GenState_734_ {
    data object Idle : GenState_734_()
    data object Loading : GenState_734_()
    data class Success(val items: List<GenModel_734_>) : GenState_734_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_734_()
    data class Partial(val items: List<GenModel_734_>, val hasMore: Boolean) : GenState_734_()
}

interface GenRepository_734_ {
    suspend fun getAll(): List<GenModel_734_>
    suspend fun getById(id: Long): GenModel_734_?
    suspend fun save(model: GenModel_734_): GenModel_734_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_734_>
}

@Singleton
class GenRepositoryImpl_734_ @Inject constructor() : GenRepository_734_ {
    private val store = mutableMapOf<Long, GenModel_734_>()
    override suspend fun getAll(): List<GenModel_734_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_734_? = store[id]
    override suspend fun save(model: GenModel_734_): GenModel_734_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_734_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_734_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_734_ @Inject constructor(
    private val repository: GenRepositoryImpl_734_
) : GenUseCase_734_<Unit, List<GenModel_734_>> {
    override suspend fun invoke(params: Unit): List<GenModel_734_> = repository.getAll()
}

class GenSaveUseCase_734_ @Inject constructor(
    private val repository: GenRepositoryImpl_734_
) : GenUseCase_734_<GenModel_734_, GenModel_734_> {
    override suspend fun invoke(params: GenModel_734_): GenModel_734_ = repository.save(params)
}

class GenDeleteUseCase_734_ @Inject constructor(
    private val repository: GenRepositoryImpl_734_
) : GenUseCase_734_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_734_ @Inject constructor(
    private val repository: GenRepositoryImpl_734_
) : GenUseCase_734_<String, List<GenModel_734_>> {
    override suspend fun invoke(params: String): List<GenModel_734_> = repository.search(params)
}

abstract class GenMapper_734_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_734_ : GenMapper_734_<GenModel_734_, String>() {
    override fun map(input: GenModel_734_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_734_ : GenMapper_734_<String, GenModel_734_>() {
    override fun map(input: String): GenModel_734_ {
        val parts = input.split(":")
        return GenModel_734_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_734_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_734_,
    private val saveUseCase: GenSaveUseCase_734_,
    private val deleteUseCase: GenDeleteUseCase_734_,
    private val searchUseCase: GenSearchUseCase_734_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_734_>(GenState_734_.Idle)
    val state: StateFlow<GenState_734_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_734_) {
        when (event) {
            is GenEvent_734_.Load -> loadAll()
            is GenEvent_734_.Update -> save(event.model)
            is GenEvent_734_.Delete -> delete(event.id)
            is GenEvent_734_.Refresh -> loadAll()
            is GenEvent_734_.Search -> search(event.query)
            is GenEvent_734_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_734_.Loading; _state.value = GenState_734_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_734_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_734_.Success(searchUseCase(query)) } }
}
