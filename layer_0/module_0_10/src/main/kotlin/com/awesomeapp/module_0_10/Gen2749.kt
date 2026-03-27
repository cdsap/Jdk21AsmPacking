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

data class GenModel_2749_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2749_ {
    data class Load(val id: Long) : GenEvent_2749_()
    data class Update(val model: GenModel_2749_) : GenEvent_2749_()
    data class Delete(val id: Long) : GenEvent_2749_()
    data object Refresh : GenEvent_2749_()
    data class Search(val query: String) : GenEvent_2749_()
    data class Filter(val predicate: String) : GenEvent_2749_()
}

sealed class GenState_2749_ {
    data object Idle : GenState_2749_()
    data object Loading : GenState_2749_()
    data class Success(val items: List<GenModel_2749_>) : GenState_2749_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2749_()
    data class Partial(val items: List<GenModel_2749_>, val hasMore: Boolean) : GenState_2749_()
}

interface GenRepository_2749_ {
    suspend fun getAll(): List<GenModel_2749_>
    suspend fun getById(id: Long): GenModel_2749_?
    suspend fun save(model: GenModel_2749_): GenModel_2749_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2749_>
}

@Singleton
class GenRepositoryImpl_2749_ @Inject constructor() : GenRepository_2749_ {
    private val store = mutableMapOf<Long, GenModel_2749_>()
    override suspend fun getAll(): List<GenModel_2749_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2749_? = store[id]
    override suspend fun save(model: GenModel_2749_): GenModel_2749_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2749_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2749_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2749_ @Inject constructor(
    private val repository: GenRepositoryImpl_2749_
) : GenUseCase_2749_<Unit, List<GenModel_2749_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2749_> = repository.getAll()
}

class GenSaveUseCase_2749_ @Inject constructor(
    private val repository: GenRepositoryImpl_2749_
) : GenUseCase_2749_<GenModel_2749_, GenModel_2749_> {
    override suspend fun invoke(params: GenModel_2749_): GenModel_2749_ = repository.save(params)
}

class GenDeleteUseCase_2749_ @Inject constructor(
    private val repository: GenRepositoryImpl_2749_
) : GenUseCase_2749_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2749_ @Inject constructor(
    private val repository: GenRepositoryImpl_2749_
) : GenUseCase_2749_<String, List<GenModel_2749_>> {
    override suspend fun invoke(params: String): List<GenModel_2749_> = repository.search(params)
}

abstract class GenMapper_2749_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2749_ : GenMapper_2749_<GenModel_2749_, String>() {
    override fun map(input: GenModel_2749_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2749_ : GenMapper_2749_<String, GenModel_2749_>() {
    override fun map(input: String): GenModel_2749_ {
        val parts = input.split(":")
        return GenModel_2749_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2749_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2749_,
    private val saveUseCase: GenSaveUseCase_2749_,
    private val deleteUseCase: GenDeleteUseCase_2749_,
    private val searchUseCase: GenSearchUseCase_2749_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2749_>(GenState_2749_.Idle)
    val state: StateFlow<GenState_2749_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2749_) {
        when (event) {
            is GenEvent_2749_.Load -> loadAll()
            is GenEvent_2749_.Update -> save(event.model)
            is GenEvent_2749_.Delete -> delete(event.id)
            is GenEvent_2749_.Refresh -> loadAll()
            is GenEvent_2749_.Search -> search(event.query)
            is GenEvent_2749_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2749_.Loading; _state.value = GenState_2749_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2749_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2749_.Success(searchUseCase(query)) } }
}
