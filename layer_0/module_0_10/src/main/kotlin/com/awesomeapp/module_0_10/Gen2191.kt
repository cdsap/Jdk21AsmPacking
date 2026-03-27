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

data class GenModel_2191_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2191_ {
    data class Load(val id: Long) : GenEvent_2191_()
    data class Update(val model: GenModel_2191_) : GenEvent_2191_()
    data class Delete(val id: Long) : GenEvent_2191_()
    data object Refresh : GenEvent_2191_()
    data class Search(val query: String) : GenEvent_2191_()
    data class Filter(val predicate: String) : GenEvent_2191_()
}

sealed class GenState_2191_ {
    data object Idle : GenState_2191_()
    data object Loading : GenState_2191_()
    data class Success(val items: List<GenModel_2191_>) : GenState_2191_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2191_()
    data class Partial(val items: List<GenModel_2191_>, val hasMore: Boolean) : GenState_2191_()
}

interface GenRepository_2191_ {
    suspend fun getAll(): List<GenModel_2191_>
    suspend fun getById(id: Long): GenModel_2191_?
    suspend fun save(model: GenModel_2191_): GenModel_2191_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2191_>
}

@Singleton
class GenRepositoryImpl_2191_ @Inject constructor() : GenRepository_2191_ {
    private val store = mutableMapOf<Long, GenModel_2191_>()
    override suspend fun getAll(): List<GenModel_2191_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2191_? = store[id]
    override suspend fun save(model: GenModel_2191_): GenModel_2191_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2191_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2191_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2191_ @Inject constructor(
    private val repository: GenRepositoryImpl_2191_
) : GenUseCase_2191_<Unit, List<GenModel_2191_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2191_> = repository.getAll()
}

class GenSaveUseCase_2191_ @Inject constructor(
    private val repository: GenRepositoryImpl_2191_
) : GenUseCase_2191_<GenModel_2191_, GenModel_2191_> {
    override suspend fun invoke(params: GenModel_2191_): GenModel_2191_ = repository.save(params)
}

class GenDeleteUseCase_2191_ @Inject constructor(
    private val repository: GenRepositoryImpl_2191_
) : GenUseCase_2191_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2191_ @Inject constructor(
    private val repository: GenRepositoryImpl_2191_
) : GenUseCase_2191_<String, List<GenModel_2191_>> {
    override suspend fun invoke(params: String): List<GenModel_2191_> = repository.search(params)
}

abstract class GenMapper_2191_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2191_ : GenMapper_2191_<GenModel_2191_, String>() {
    override fun map(input: GenModel_2191_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2191_ : GenMapper_2191_<String, GenModel_2191_>() {
    override fun map(input: String): GenModel_2191_ {
        val parts = input.split(":")
        return GenModel_2191_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2191_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2191_,
    private val saveUseCase: GenSaveUseCase_2191_,
    private val deleteUseCase: GenDeleteUseCase_2191_,
    private val searchUseCase: GenSearchUseCase_2191_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2191_>(GenState_2191_.Idle)
    val state: StateFlow<GenState_2191_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2191_) {
        when (event) {
            is GenEvent_2191_.Load -> loadAll()
            is GenEvent_2191_.Update -> save(event.model)
            is GenEvent_2191_.Delete -> delete(event.id)
            is GenEvent_2191_.Refresh -> loadAll()
            is GenEvent_2191_.Search -> search(event.query)
            is GenEvent_2191_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2191_.Loading; _state.value = GenState_2191_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2191_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2191_.Success(searchUseCase(query)) } }
}
