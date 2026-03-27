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

data class GenModel_523_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_523_ {
    data class Load(val id: Long) : GenEvent_523_()
    data class Update(val model: GenModel_523_) : GenEvent_523_()
    data class Delete(val id: Long) : GenEvent_523_()
    data object Refresh : GenEvent_523_()
    data class Search(val query: String) : GenEvent_523_()
    data class Filter(val predicate: String) : GenEvent_523_()
}

sealed class GenState_523_ {
    data object Idle : GenState_523_()
    data object Loading : GenState_523_()
    data class Success(val items: List<GenModel_523_>) : GenState_523_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_523_()
    data class Partial(val items: List<GenModel_523_>, val hasMore: Boolean) : GenState_523_()
}

interface GenRepository_523_ {
    suspend fun getAll(): List<GenModel_523_>
    suspend fun getById(id: Long): GenModel_523_?
    suspend fun save(model: GenModel_523_): GenModel_523_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_523_>
}

@Singleton
class GenRepositoryImpl_523_ @Inject constructor() : GenRepository_523_ {
    private val store = mutableMapOf<Long, GenModel_523_>()
    override suspend fun getAll(): List<GenModel_523_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_523_? = store[id]
    override suspend fun save(model: GenModel_523_): GenModel_523_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_523_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_523_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_523_ @Inject constructor(
    private val repository: GenRepositoryImpl_523_
) : GenUseCase_523_<Unit, List<GenModel_523_>> {
    override suspend fun invoke(params: Unit): List<GenModel_523_> = repository.getAll()
}

class GenSaveUseCase_523_ @Inject constructor(
    private val repository: GenRepositoryImpl_523_
) : GenUseCase_523_<GenModel_523_, GenModel_523_> {
    override suspend fun invoke(params: GenModel_523_): GenModel_523_ = repository.save(params)
}

class GenDeleteUseCase_523_ @Inject constructor(
    private val repository: GenRepositoryImpl_523_
) : GenUseCase_523_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_523_ @Inject constructor(
    private val repository: GenRepositoryImpl_523_
) : GenUseCase_523_<String, List<GenModel_523_>> {
    override suspend fun invoke(params: String): List<GenModel_523_> = repository.search(params)
}

abstract class GenMapper_523_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_523_ : GenMapper_523_<GenModel_523_, String>() {
    override fun map(input: GenModel_523_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_523_ : GenMapper_523_<String, GenModel_523_>() {
    override fun map(input: String): GenModel_523_ {
        val parts = input.split(":")
        return GenModel_523_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_523_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_523_,
    private val saveUseCase: GenSaveUseCase_523_,
    private val deleteUseCase: GenDeleteUseCase_523_,
    private val searchUseCase: GenSearchUseCase_523_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_523_>(GenState_523_.Idle)
    val state: StateFlow<GenState_523_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_523_) {
        when (event) {
            is GenEvent_523_.Load -> loadAll()
            is GenEvent_523_.Update -> save(event.model)
            is GenEvent_523_.Delete -> delete(event.id)
            is GenEvent_523_.Refresh -> loadAll()
            is GenEvent_523_.Search -> search(event.query)
            is GenEvent_523_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_523_.Loading; _state.value = GenState_523_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_523_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_523_.Success(searchUseCase(query)) } }
}
