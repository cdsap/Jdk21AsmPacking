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

data class GenModel_856_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_856_ {
    data class Load(val id: Long) : GenEvent_856_()
    data class Update(val model: GenModel_856_) : GenEvent_856_()
    data class Delete(val id: Long) : GenEvent_856_()
    data object Refresh : GenEvent_856_()
    data class Search(val query: String) : GenEvent_856_()
    data class Filter(val predicate: String) : GenEvent_856_()
}

sealed class GenState_856_ {
    data object Idle : GenState_856_()
    data object Loading : GenState_856_()
    data class Success(val items: List<GenModel_856_>) : GenState_856_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_856_()
    data class Partial(val items: List<GenModel_856_>, val hasMore: Boolean) : GenState_856_()
}

interface GenRepository_856_ {
    suspend fun getAll(): List<GenModel_856_>
    suspend fun getById(id: Long): GenModel_856_?
    suspend fun save(model: GenModel_856_): GenModel_856_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_856_>
}

@Singleton
class GenRepositoryImpl_856_ @Inject constructor() : GenRepository_856_ {
    private val store = mutableMapOf<Long, GenModel_856_>()
    override suspend fun getAll(): List<GenModel_856_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_856_? = store[id]
    override suspend fun save(model: GenModel_856_): GenModel_856_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_856_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_856_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_856_ @Inject constructor(
    private val repository: GenRepositoryImpl_856_
) : GenUseCase_856_<Unit, List<GenModel_856_>> {
    override suspend fun invoke(params: Unit): List<GenModel_856_> = repository.getAll()
}

class GenSaveUseCase_856_ @Inject constructor(
    private val repository: GenRepositoryImpl_856_
) : GenUseCase_856_<GenModel_856_, GenModel_856_> {
    override suspend fun invoke(params: GenModel_856_): GenModel_856_ = repository.save(params)
}

class GenDeleteUseCase_856_ @Inject constructor(
    private val repository: GenRepositoryImpl_856_
) : GenUseCase_856_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_856_ @Inject constructor(
    private val repository: GenRepositoryImpl_856_
) : GenUseCase_856_<String, List<GenModel_856_>> {
    override suspend fun invoke(params: String): List<GenModel_856_> = repository.search(params)
}

abstract class GenMapper_856_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_856_ : GenMapper_856_<GenModel_856_, String>() {
    override fun map(input: GenModel_856_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_856_ : GenMapper_856_<String, GenModel_856_>() {
    override fun map(input: String): GenModel_856_ {
        val parts = input.split(":")
        return GenModel_856_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_856_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_856_,
    private val saveUseCase: GenSaveUseCase_856_,
    private val deleteUseCase: GenDeleteUseCase_856_,
    private val searchUseCase: GenSearchUseCase_856_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_856_>(GenState_856_.Idle)
    val state: StateFlow<GenState_856_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_856_) {
        when (event) {
            is GenEvent_856_.Load -> loadAll()
            is GenEvent_856_.Update -> save(event.model)
            is GenEvent_856_.Delete -> delete(event.id)
            is GenEvent_856_.Refresh -> loadAll()
            is GenEvent_856_.Search -> search(event.query)
            is GenEvent_856_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_856_.Loading; _state.value = GenState_856_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_856_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_856_.Success(searchUseCase(query)) } }
}
