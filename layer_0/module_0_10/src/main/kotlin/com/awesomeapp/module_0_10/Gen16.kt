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

data class GenModel_16_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_16_ {
    data class Load(val id: Long) : GenEvent_16_()
    data class Update(val model: GenModel_16_) : GenEvent_16_()
    data class Delete(val id: Long) : GenEvent_16_()
    data object Refresh : GenEvent_16_()
    data class Search(val query: String) : GenEvent_16_()
    data class Filter(val predicate: String) : GenEvent_16_()
}

sealed class GenState_16_ {
    data object Idle : GenState_16_()
    data object Loading : GenState_16_()
    data class Success(val items: List<GenModel_16_>) : GenState_16_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_16_()
    data class Partial(val items: List<GenModel_16_>, val hasMore: Boolean) : GenState_16_()
}

interface GenRepository_16_ {
    suspend fun getAll(): List<GenModel_16_>
    suspend fun getById(id: Long): GenModel_16_?
    suspend fun save(model: GenModel_16_): GenModel_16_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_16_>
}

@Singleton
class GenRepositoryImpl_16_ @Inject constructor() : GenRepository_16_ {
    private val store = mutableMapOf<Long, GenModel_16_>()
    override suspend fun getAll(): List<GenModel_16_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_16_? = store[id]
    override suspend fun save(model: GenModel_16_): GenModel_16_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_16_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_16_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_16_ @Inject constructor(
    private val repository: GenRepositoryImpl_16_
) : GenUseCase_16_<Unit, List<GenModel_16_>> {
    override suspend fun invoke(params: Unit): List<GenModel_16_> = repository.getAll()
}

class GenSaveUseCase_16_ @Inject constructor(
    private val repository: GenRepositoryImpl_16_
) : GenUseCase_16_<GenModel_16_, GenModel_16_> {
    override suspend fun invoke(params: GenModel_16_): GenModel_16_ = repository.save(params)
}

class GenDeleteUseCase_16_ @Inject constructor(
    private val repository: GenRepositoryImpl_16_
) : GenUseCase_16_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_16_ @Inject constructor(
    private val repository: GenRepositoryImpl_16_
) : GenUseCase_16_<String, List<GenModel_16_>> {
    override suspend fun invoke(params: String): List<GenModel_16_> = repository.search(params)
}

abstract class GenMapper_16_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_16_ : GenMapper_16_<GenModel_16_, String>() {
    override fun map(input: GenModel_16_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_16_ : GenMapper_16_<String, GenModel_16_>() {
    override fun map(input: String): GenModel_16_ {
        val parts = input.split(":")
        return GenModel_16_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_16_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_16_,
    private val saveUseCase: GenSaveUseCase_16_,
    private val deleteUseCase: GenDeleteUseCase_16_,
    private val searchUseCase: GenSearchUseCase_16_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_16_>(GenState_16_.Idle)
    val state: StateFlow<GenState_16_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_16_) {
        when (event) {
            is GenEvent_16_.Load -> loadAll()
            is GenEvent_16_.Update -> save(event.model)
            is GenEvent_16_.Delete -> delete(event.id)
            is GenEvent_16_.Refresh -> loadAll()
            is GenEvent_16_.Search -> search(event.query)
            is GenEvent_16_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_16_.Loading; _state.value = GenState_16_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_16_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_16_.Success(searchUseCase(query)) } }
}
