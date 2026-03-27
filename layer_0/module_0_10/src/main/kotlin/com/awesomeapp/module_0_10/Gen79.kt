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

data class GenModel_79_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_79_ {
    data class Load(val id: Long) : GenEvent_79_()
    data class Update(val model: GenModel_79_) : GenEvent_79_()
    data class Delete(val id: Long) : GenEvent_79_()
    data object Refresh : GenEvent_79_()
    data class Search(val query: String) : GenEvent_79_()
    data class Filter(val predicate: String) : GenEvent_79_()
}

sealed class GenState_79_ {
    data object Idle : GenState_79_()
    data object Loading : GenState_79_()
    data class Success(val items: List<GenModel_79_>) : GenState_79_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_79_()
    data class Partial(val items: List<GenModel_79_>, val hasMore: Boolean) : GenState_79_()
}

interface GenRepository_79_ {
    suspend fun getAll(): List<GenModel_79_>
    suspend fun getById(id: Long): GenModel_79_?
    suspend fun save(model: GenModel_79_): GenModel_79_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_79_>
}

@Singleton
class GenRepositoryImpl_79_ @Inject constructor() : GenRepository_79_ {
    private val store = mutableMapOf<Long, GenModel_79_>()
    override suspend fun getAll(): List<GenModel_79_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_79_? = store[id]
    override suspend fun save(model: GenModel_79_): GenModel_79_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_79_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_79_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_79_ @Inject constructor(
    private val repository: GenRepositoryImpl_79_
) : GenUseCase_79_<Unit, List<GenModel_79_>> {
    override suspend fun invoke(params: Unit): List<GenModel_79_> = repository.getAll()
}

class GenSaveUseCase_79_ @Inject constructor(
    private val repository: GenRepositoryImpl_79_
) : GenUseCase_79_<GenModel_79_, GenModel_79_> {
    override suspend fun invoke(params: GenModel_79_): GenModel_79_ = repository.save(params)
}

class GenDeleteUseCase_79_ @Inject constructor(
    private val repository: GenRepositoryImpl_79_
) : GenUseCase_79_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_79_ @Inject constructor(
    private val repository: GenRepositoryImpl_79_
) : GenUseCase_79_<String, List<GenModel_79_>> {
    override suspend fun invoke(params: String): List<GenModel_79_> = repository.search(params)
}

abstract class GenMapper_79_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_79_ : GenMapper_79_<GenModel_79_, String>() {
    override fun map(input: GenModel_79_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_79_ : GenMapper_79_<String, GenModel_79_>() {
    override fun map(input: String): GenModel_79_ {
        val parts = input.split(":")
        return GenModel_79_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_79_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_79_,
    private val saveUseCase: GenSaveUseCase_79_,
    private val deleteUseCase: GenDeleteUseCase_79_,
    private val searchUseCase: GenSearchUseCase_79_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_79_>(GenState_79_.Idle)
    val state: StateFlow<GenState_79_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_79_) {
        when (event) {
            is GenEvent_79_.Load -> loadAll()
            is GenEvent_79_.Update -> save(event.model)
            is GenEvent_79_.Delete -> delete(event.id)
            is GenEvent_79_.Refresh -> loadAll()
            is GenEvent_79_.Search -> search(event.query)
            is GenEvent_79_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_79_.Loading; _state.value = GenState_79_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_79_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_79_.Success(searchUseCase(query)) } }
}
