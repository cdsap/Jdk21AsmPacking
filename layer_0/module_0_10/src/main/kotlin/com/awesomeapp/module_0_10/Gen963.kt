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

data class GenModel_963_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_963_ {
    data class Load(val id: Long) : GenEvent_963_()
    data class Update(val model: GenModel_963_) : GenEvent_963_()
    data class Delete(val id: Long) : GenEvent_963_()
    data object Refresh : GenEvent_963_()
    data class Search(val query: String) : GenEvent_963_()
    data class Filter(val predicate: String) : GenEvent_963_()
}

sealed class GenState_963_ {
    data object Idle : GenState_963_()
    data object Loading : GenState_963_()
    data class Success(val items: List<GenModel_963_>) : GenState_963_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_963_()
    data class Partial(val items: List<GenModel_963_>, val hasMore: Boolean) : GenState_963_()
}

interface GenRepository_963_ {
    suspend fun getAll(): List<GenModel_963_>
    suspend fun getById(id: Long): GenModel_963_?
    suspend fun save(model: GenModel_963_): GenModel_963_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_963_>
}

@Singleton
class GenRepositoryImpl_963_ @Inject constructor() : GenRepository_963_ {
    private val store = mutableMapOf<Long, GenModel_963_>()
    override suspend fun getAll(): List<GenModel_963_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_963_? = store[id]
    override suspend fun save(model: GenModel_963_): GenModel_963_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_963_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_963_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_963_ @Inject constructor(
    private val repository: GenRepositoryImpl_963_
) : GenUseCase_963_<Unit, List<GenModel_963_>> {
    override suspend fun invoke(params: Unit): List<GenModel_963_> = repository.getAll()
}

class GenSaveUseCase_963_ @Inject constructor(
    private val repository: GenRepositoryImpl_963_
) : GenUseCase_963_<GenModel_963_, GenModel_963_> {
    override suspend fun invoke(params: GenModel_963_): GenModel_963_ = repository.save(params)
}

class GenDeleteUseCase_963_ @Inject constructor(
    private val repository: GenRepositoryImpl_963_
) : GenUseCase_963_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_963_ @Inject constructor(
    private val repository: GenRepositoryImpl_963_
) : GenUseCase_963_<String, List<GenModel_963_>> {
    override suspend fun invoke(params: String): List<GenModel_963_> = repository.search(params)
}

abstract class GenMapper_963_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_963_ : GenMapper_963_<GenModel_963_, String>() {
    override fun map(input: GenModel_963_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_963_ : GenMapper_963_<String, GenModel_963_>() {
    override fun map(input: String): GenModel_963_ {
        val parts = input.split(":")
        return GenModel_963_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_963_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_963_,
    private val saveUseCase: GenSaveUseCase_963_,
    private val deleteUseCase: GenDeleteUseCase_963_,
    private val searchUseCase: GenSearchUseCase_963_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_963_>(GenState_963_.Idle)
    val state: StateFlow<GenState_963_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_963_) {
        when (event) {
            is GenEvent_963_.Load -> loadAll()
            is GenEvent_963_.Update -> save(event.model)
            is GenEvent_963_.Delete -> delete(event.id)
            is GenEvent_963_.Refresh -> loadAll()
            is GenEvent_963_.Search -> search(event.query)
            is GenEvent_963_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_963_.Loading; _state.value = GenState_963_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_963_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_963_.Success(searchUseCase(query)) } }
}
