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

data class GenModel_900_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_900_ {
    data class Load(val id: Long) : GenEvent_900_()
    data class Update(val model: GenModel_900_) : GenEvent_900_()
    data class Delete(val id: Long) : GenEvent_900_()
    data object Refresh : GenEvent_900_()
    data class Search(val query: String) : GenEvent_900_()
    data class Filter(val predicate: String) : GenEvent_900_()
}

sealed class GenState_900_ {
    data object Idle : GenState_900_()
    data object Loading : GenState_900_()
    data class Success(val items: List<GenModel_900_>) : GenState_900_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_900_()
    data class Partial(val items: List<GenModel_900_>, val hasMore: Boolean) : GenState_900_()
}

interface GenRepository_900_ {
    suspend fun getAll(): List<GenModel_900_>
    suspend fun getById(id: Long): GenModel_900_?
    suspend fun save(model: GenModel_900_): GenModel_900_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_900_>
}

@Singleton
class GenRepositoryImpl_900_ @Inject constructor() : GenRepository_900_ {
    private val store = mutableMapOf<Long, GenModel_900_>()
    override suspend fun getAll(): List<GenModel_900_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_900_? = store[id]
    override suspend fun save(model: GenModel_900_): GenModel_900_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_900_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_900_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_900_ @Inject constructor(
    private val repository: GenRepositoryImpl_900_
) : GenUseCase_900_<Unit, List<GenModel_900_>> {
    override suspend fun invoke(params: Unit): List<GenModel_900_> = repository.getAll()
}

class GenSaveUseCase_900_ @Inject constructor(
    private val repository: GenRepositoryImpl_900_
) : GenUseCase_900_<GenModel_900_, GenModel_900_> {
    override suspend fun invoke(params: GenModel_900_): GenModel_900_ = repository.save(params)
}

class GenDeleteUseCase_900_ @Inject constructor(
    private val repository: GenRepositoryImpl_900_
) : GenUseCase_900_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_900_ @Inject constructor(
    private val repository: GenRepositoryImpl_900_
) : GenUseCase_900_<String, List<GenModel_900_>> {
    override suspend fun invoke(params: String): List<GenModel_900_> = repository.search(params)
}

abstract class GenMapper_900_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_900_ : GenMapper_900_<GenModel_900_, String>() {
    override fun map(input: GenModel_900_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_900_ : GenMapper_900_<String, GenModel_900_>() {
    override fun map(input: String): GenModel_900_ {
        val parts = input.split(":")
        return GenModel_900_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_900_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_900_,
    private val saveUseCase: GenSaveUseCase_900_,
    private val deleteUseCase: GenDeleteUseCase_900_,
    private val searchUseCase: GenSearchUseCase_900_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_900_>(GenState_900_.Idle)
    val state: StateFlow<GenState_900_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_900_) {
        when (event) {
            is GenEvent_900_.Load -> loadAll()
            is GenEvent_900_.Update -> save(event.model)
            is GenEvent_900_.Delete -> delete(event.id)
            is GenEvent_900_.Refresh -> loadAll()
            is GenEvent_900_.Search -> search(event.query)
            is GenEvent_900_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_900_.Loading; _state.value = GenState_900_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_900_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_900_.Success(searchUseCase(query)) } }
}
