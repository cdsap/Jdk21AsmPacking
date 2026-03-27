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

data class GenModel_811_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_811_ {
    data class Load(val id: Long) : GenEvent_811_()
    data class Update(val model: GenModel_811_) : GenEvent_811_()
    data class Delete(val id: Long) : GenEvent_811_()
    data object Refresh : GenEvent_811_()
    data class Search(val query: String) : GenEvent_811_()
    data class Filter(val predicate: String) : GenEvent_811_()
}

sealed class GenState_811_ {
    data object Idle : GenState_811_()
    data object Loading : GenState_811_()
    data class Success(val items: List<GenModel_811_>) : GenState_811_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_811_()
    data class Partial(val items: List<GenModel_811_>, val hasMore: Boolean) : GenState_811_()
}

interface GenRepository_811_ {
    suspend fun getAll(): List<GenModel_811_>
    suspend fun getById(id: Long): GenModel_811_?
    suspend fun save(model: GenModel_811_): GenModel_811_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_811_>
}

@Singleton
class GenRepositoryImpl_811_ @Inject constructor() : GenRepository_811_ {
    private val store = mutableMapOf<Long, GenModel_811_>()
    override suspend fun getAll(): List<GenModel_811_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_811_? = store[id]
    override suspend fun save(model: GenModel_811_): GenModel_811_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_811_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_811_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_811_ @Inject constructor(
    private val repository: GenRepositoryImpl_811_
) : GenUseCase_811_<Unit, List<GenModel_811_>> {
    override suspend fun invoke(params: Unit): List<GenModel_811_> = repository.getAll()
}

class GenSaveUseCase_811_ @Inject constructor(
    private val repository: GenRepositoryImpl_811_
) : GenUseCase_811_<GenModel_811_, GenModel_811_> {
    override suspend fun invoke(params: GenModel_811_): GenModel_811_ = repository.save(params)
}

class GenDeleteUseCase_811_ @Inject constructor(
    private val repository: GenRepositoryImpl_811_
) : GenUseCase_811_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_811_ @Inject constructor(
    private val repository: GenRepositoryImpl_811_
) : GenUseCase_811_<String, List<GenModel_811_>> {
    override suspend fun invoke(params: String): List<GenModel_811_> = repository.search(params)
}

abstract class GenMapper_811_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_811_ : GenMapper_811_<GenModel_811_, String>() {
    override fun map(input: GenModel_811_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_811_ : GenMapper_811_<String, GenModel_811_>() {
    override fun map(input: String): GenModel_811_ {
        val parts = input.split(":")
        return GenModel_811_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_811_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_811_,
    private val saveUseCase: GenSaveUseCase_811_,
    private val deleteUseCase: GenDeleteUseCase_811_,
    private val searchUseCase: GenSearchUseCase_811_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_811_>(GenState_811_.Idle)
    val state: StateFlow<GenState_811_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_811_) {
        when (event) {
            is GenEvent_811_.Load -> loadAll()
            is GenEvent_811_.Update -> save(event.model)
            is GenEvent_811_.Delete -> delete(event.id)
            is GenEvent_811_.Refresh -> loadAll()
            is GenEvent_811_.Search -> search(event.query)
            is GenEvent_811_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_811_.Loading; _state.value = GenState_811_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_811_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_811_.Success(searchUseCase(query)) } }
}
