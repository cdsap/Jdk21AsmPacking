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

data class GenModel_1839_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1839_ {
    data class Load(val id: Long) : GenEvent_1839_()
    data class Update(val model: GenModel_1839_) : GenEvent_1839_()
    data class Delete(val id: Long) : GenEvent_1839_()
    data object Refresh : GenEvent_1839_()
    data class Search(val query: String) : GenEvent_1839_()
    data class Filter(val predicate: String) : GenEvent_1839_()
}

sealed class GenState_1839_ {
    data object Idle : GenState_1839_()
    data object Loading : GenState_1839_()
    data class Success(val items: List<GenModel_1839_>) : GenState_1839_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1839_()
    data class Partial(val items: List<GenModel_1839_>, val hasMore: Boolean) : GenState_1839_()
}

interface GenRepository_1839_ {
    suspend fun getAll(): List<GenModel_1839_>
    suspend fun getById(id: Long): GenModel_1839_?
    suspend fun save(model: GenModel_1839_): GenModel_1839_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1839_>
}

@Singleton
class GenRepositoryImpl_1839_ @Inject constructor() : GenRepository_1839_ {
    private val store = mutableMapOf<Long, GenModel_1839_>()
    override suspend fun getAll(): List<GenModel_1839_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1839_? = store[id]
    override suspend fun save(model: GenModel_1839_): GenModel_1839_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1839_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1839_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1839_ @Inject constructor(
    private val repository: GenRepositoryImpl_1839_
) : GenUseCase_1839_<Unit, List<GenModel_1839_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1839_> = repository.getAll()
}

class GenSaveUseCase_1839_ @Inject constructor(
    private val repository: GenRepositoryImpl_1839_
) : GenUseCase_1839_<GenModel_1839_, GenModel_1839_> {
    override suspend fun invoke(params: GenModel_1839_): GenModel_1839_ = repository.save(params)
}

class GenDeleteUseCase_1839_ @Inject constructor(
    private val repository: GenRepositoryImpl_1839_
) : GenUseCase_1839_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1839_ @Inject constructor(
    private val repository: GenRepositoryImpl_1839_
) : GenUseCase_1839_<String, List<GenModel_1839_>> {
    override suspend fun invoke(params: String): List<GenModel_1839_> = repository.search(params)
}

abstract class GenMapper_1839_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1839_ : GenMapper_1839_<GenModel_1839_, String>() {
    override fun map(input: GenModel_1839_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1839_ : GenMapper_1839_<String, GenModel_1839_>() {
    override fun map(input: String): GenModel_1839_ {
        val parts = input.split(":")
        return GenModel_1839_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1839_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1839_,
    private val saveUseCase: GenSaveUseCase_1839_,
    private val deleteUseCase: GenDeleteUseCase_1839_,
    private val searchUseCase: GenSearchUseCase_1839_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1839_>(GenState_1839_.Idle)
    val state: StateFlow<GenState_1839_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1839_) {
        when (event) {
            is GenEvent_1839_.Load -> loadAll()
            is GenEvent_1839_.Update -> save(event.model)
            is GenEvent_1839_.Delete -> delete(event.id)
            is GenEvent_1839_.Refresh -> loadAll()
            is GenEvent_1839_.Search -> search(event.query)
            is GenEvent_1839_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1839_.Loading; _state.value = GenState_1839_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1839_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1839_.Success(searchUseCase(query)) } }
}
