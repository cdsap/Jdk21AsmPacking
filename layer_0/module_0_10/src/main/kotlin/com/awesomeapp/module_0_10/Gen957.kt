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

data class GenModel_957_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_957_ {
    data class Load(val id: Long) : GenEvent_957_()
    data class Update(val model: GenModel_957_) : GenEvent_957_()
    data class Delete(val id: Long) : GenEvent_957_()
    data object Refresh : GenEvent_957_()
    data class Search(val query: String) : GenEvent_957_()
    data class Filter(val predicate: String) : GenEvent_957_()
}

sealed class GenState_957_ {
    data object Idle : GenState_957_()
    data object Loading : GenState_957_()
    data class Success(val items: List<GenModel_957_>) : GenState_957_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_957_()
    data class Partial(val items: List<GenModel_957_>, val hasMore: Boolean) : GenState_957_()
}

interface GenRepository_957_ {
    suspend fun getAll(): List<GenModel_957_>
    suspend fun getById(id: Long): GenModel_957_?
    suspend fun save(model: GenModel_957_): GenModel_957_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_957_>
}

@Singleton
class GenRepositoryImpl_957_ @Inject constructor() : GenRepository_957_ {
    private val store = mutableMapOf<Long, GenModel_957_>()
    override suspend fun getAll(): List<GenModel_957_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_957_? = store[id]
    override suspend fun save(model: GenModel_957_): GenModel_957_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_957_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_957_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_957_ @Inject constructor(
    private val repository: GenRepositoryImpl_957_
) : GenUseCase_957_<Unit, List<GenModel_957_>> {
    override suspend fun invoke(params: Unit): List<GenModel_957_> = repository.getAll()
}

class GenSaveUseCase_957_ @Inject constructor(
    private val repository: GenRepositoryImpl_957_
) : GenUseCase_957_<GenModel_957_, GenModel_957_> {
    override suspend fun invoke(params: GenModel_957_): GenModel_957_ = repository.save(params)
}

class GenDeleteUseCase_957_ @Inject constructor(
    private val repository: GenRepositoryImpl_957_
) : GenUseCase_957_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_957_ @Inject constructor(
    private val repository: GenRepositoryImpl_957_
) : GenUseCase_957_<String, List<GenModel_957_>> {
    override suspend fun invoke(params: String): List<GenModel_957_> = repository.search(params)
}

abstract class GenMapper_957_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_957_ : GenMapper_957_<GenModel_957_, String>() {
    override fun map(input: GenModel_957_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_957_ : GenMapper_957_<String, GenModel_957_>() {
    override fun map(input: String): GenModel_957_ {
        val parts = input.split(":")
        return GenModel_957_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_957_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_957_,
    private val saveUseCase: GenSaveUseCase_957_,
    private val deleteUseCase: GenDeleteUseCase_957_,
    private val searchUseCase: GenSearchUseCase_957_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_957_>(GenState_957_.Idle)
    val state: StateFlow<GenState_957_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_957_) {
        when (event) {
            is GenEvent_957_.Load -> loadAll()
            is GenEvent_957_.Update -> save(event.model)
            is GenEvent_957_.Delete -> delete(event.id)
            is GenEvent_957_.Refresh -> loadAll()
            is GenEvent_957_.Search -> search(event.query)
            is GenEvent_957_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_957_.Loading; _state.value = GenState_957_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_957_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_957_.Success(searchUseCase(query)) } }
}
