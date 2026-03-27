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

data class GenModel_785_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_785_ {
    data class Load(val id: Long) : GenEvent_785_()
    data class Update(val model: GenModel_785_) : GenEvent_785_()
    data class Delete(val id: Long) : GenEvent_785_()
    data object Refresh : GenEvent_785_()
    data class Search(val query: String) : GenEvent_785_()
    data class Filter(val predicate: String) : GenEvent_785_()
}

sealed class GenState_785_ {
    data object Idle : GenState_785_()
    data object Loading : GenState_785_()
    data class Success(val items: List<GenModel_785_>) : GenState_785_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_785_()
    data class Partial(val items: List<GenModel_785_>, val hasMore: Boolean) : GenState_785_()
}

interface GenRepository_785_ {
    suspend fun getAll(): List<GenModel_785_>
    suspend fun getById(id: Long): GenModel_785_?
    suspend fun save(model: GenModel_785_): GenModel_785_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_785_>
}

@Singleton
class GenRepositoryImpl_785_ @Inject constructor() : GenRepository_785_ {
    private val store = mutableMapOf<Long, GenModel_785_>()
    override suspend fun getAll(): List<GenModel_785_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_785_? = store[id]
    override suspend fun save(model: GenModel_785_): GenModel_785_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_785_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_785_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_785_ @Inject constructor(
    private val repository: GenRepositoryImpl_785_
) : GenUseCase_785_<Unit, List<GenModel_785_>> {
    override suspend fun invoke(params: Unit): List<GenModel_785_> = repository.getAll()
}

class GenSaveUseCase_785_ @Inject constructor(
    private val repository: GenRepositoryImpl_785_
) : GenUseCase_785_<GenModel_785_, GenModel_785_> {
    override suspend fun invoke(params: GenModel_785_): GenModel_785_ = repository.save(params)
}

class GenDeleteUseCase_785_ @Inject constructor(
    private val repository: GenRepositoryImpl_785_
) : GenUseCase_785_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_785_ @Inject constructor(
    private val repository: GenRepositoryImpl_785_
) : GenUseCase_785_<String, List<GenModel_785_>> {
    override suspend fun invoke(params: String): List<GenModel_785_> = repository.search(params)
}

abstract class GenMapper_785_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_785_ : GenMapper_785_<GenModel_785_, String>() {
    override fun map(input: GenModel_785_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_785_ : GenMapper_785_<String, GenModel_785_>() {
    override fun map(input: String): GenModel_785_ {
        val parts = input.split(":")
        return GenModel_785_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_785_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_785_,
    private val saveUseCase: GenSaveUseCase_785_,
    private val deleteUseCase: GenDeleteUseCase_785_,
    private val searchUseCase: GenSearchUseCase_785_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_785_>(GenState_785_.Idle)
    val state: StateFlow<GenState_785_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_785_) {
        when (event) {
            is GenEvent_785_.Load -> loadAll()
            is GenEvent_785_.Update -> save(event.model)
            is GenEvent_785_.Delete -> delete(event.id)
            is GenEvent_785_.Refresh -> loadAll()
            is GenEvent_785_.Search -> search(event.query)
            is GenEvent_785_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_785_.Loading; _state.value = GenState_785_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_785_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_785_.Success(searchUseCase(query)) } }
}
