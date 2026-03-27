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

data class GenModel_341_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_341_ {
    data class Load(val id: Long) : GenEvent_341_()
    data class Update(val model: GenModel_341_) : GenEvent_341_()
    data class Delete(val id: Long) : GenEvent_341_()
    data object Refresh : GenEvent_341_()
    data class Search(val query: String) : GenEvent_341_()
    data class Filter(val predicate: String) : GenEvent_341_()
}

sealed class GenState_341_ {
    data object Idle : GenState_341_()
    data object Loading : GenState_341_()
    data class Success(val items: List<GenModel_341_>) : GenState_341_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_341_()
    data class Partial(val items: List<GenModel_341_>, val hasMore: Boolean) : GenState_341_()
}

interface GenRepository_341_ {
    suspend fun getAll(): List<GenModel_341_>
    suspend fun getById(id: Long): GenModel_341_?
    suspend fun save(model: GenModel_341_): GenModel_341_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_341_>
}

@Singleton
class GenRepositoryImpl_341_ @Inject constructor() : GenRepository_341_ {
    private val store = mutableMapOf<Long, GenModel_341_>()
    override suspend fun getAll(): List<GenModel_341_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_341_? = store[id]
    override suspend fun save(model: GenModel_341_): GenModel_341_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_341_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_341_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_341_ @Inject constructor(
    private val repository: GenRepositoryImpl_341_
) : GenUseCase_341_<Unit, List<GenModel_341_>> {
    override suspend fun invoke(params: Unit): List<GenModel_341_> = repository.getAll()
}

class GenSaveUseCase_341_ @Inject constructor(
    private val repository: GenRepositoryImpl_341_
) : GenUseCase_341_<GenModel_341_, GenModel_341_> {
    override suspend fun invoke(params: GenModel_341_): GenModel_341_ = repository.save(params)
}

class GenDeleteUseCase_341_ @Inject constructor(
    private val repository: GenRepositoryImpl_341_
) : GenUseCase_341_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_341_ @Inject constructor(
    private val repository: GenRepositoryImpl_341_
) : GenUseCase_341_<String, List<GenModel_341_>> {
    override suspend fun invoke(params: String): List<GenModel_341_> = repository.search(params)
}

abstract class GenMapper_341_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_341_ : GenMapper_341_<GenModel_341_, String>() {
    override fun map(input: GenModel_341_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_341_ : GenMapper_341_<String, GenModel_341_>() {
    override fun map(input: String): GenModel_341_ {
        val parts = input.split(":")
        return GenModel_341_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_341_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_341_,
    private val saveUseCase: GenSaveUseCase_341_,
    private val deleteUseCase: GenDeleteUseCase_341_,
    private val searchUseCase: GenSearchUseCase_341_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_341_>(GenState_341_.Idle)
    val state: StateFlow<GenState_341_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_341_) {
        when (event) {
            is GenEvent_341_.Load -> loadAll()
            is GenEvent_341_.Update -> save(event.model)
            is GenEvent_341_.Delete -> delete(event.id)
            is GenEvent_341_.Refresh -> loadAll()
            is GenEvent_341_.Search -> search(event.query)
            is GenEvent_341_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_341_.Loading; _state.value = GenState_341_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_341_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_341_.Success(searchUseCase(query)) } }
}
