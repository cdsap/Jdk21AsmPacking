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

data class GenModel_279_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_279_ {
    data class Load(val id: Long) : GenEvent_279_()
    data class Update(val model: GenModel_279_) : GenEvent_279_()
    data class Delete(val id: Long) : GenEvent_279_()
    data object Refresh : GenEvent_279_()
    data class Search(val query: String) : GenEvent_279_()
    data class Filter(val predicate: String) : GenEvent_279_()
}

sealed class GenState_279_ {
    data object Idle : GenState_279_()
    data object Loading : GenState_279_()
    data class Success(val items: List<GenModel_279_>) : GenState_279_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_279_()
    data class Partial(val items: List<GenModel_279_>, val hasMore: Boolean) : GenState_279_()
}

interface GenRepository_279_ {
    suspend fun getAll(): List<GenModel_279_>
    suspend fun getById(id: Long): GenModel_279_?
    suspend fun save(model: GenModel_279_): GenModel_279_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_279_>
}

@Singleton
class GenRepositoryImpl_279_ @Inject constructor() : GenRepository_279_ {
    private val store = mutableMapOf<Long, GenModel_279_>()
    override suspend fun getAll(): List<GenModel_279_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_279_? = store[id]
    override suspend fun save(model: GenModel_279_): GenModel_279_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_279_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_279_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_279_ @Inject constructor(
    private val repository: GenRepositoryImpl_279_
) : GenUseCase_279_<Unit, List<GenModel_279_>> {
    override suspend fun invoke(params: Unit): List<GenModel_279_> = repository.getAll()
}

class GenSaveUseCase_279_ @Inject constructor(
    private val repository: GenRepositoryImpl_279_
) : GenUseCase_279_<GenModel_279_, GenModel_279_> {
    override suspend fun invoke(params: GenModel_279_): GenModel_279_ = repository.save(params)
}

class GenDeleteUseCase_279_ @Inject constructor(
    private val repository: GenRepositoryImpl_279_
) : GenUseCase_279_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_279_ @Inject constructor(
    private val repository: GenRepositoryImpl_279_
) : GenUseCase_279_<String, List<GenModel_279_>> {
    override suspend fun invoke(params: String): List<GenModel_279_> = repository.search(params)
}

abstract class GenMapper_279_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_279_ : GenMapper_279_<GenModel_279_, String>() {
    override fun map(input: GenModel_279_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_279_ : GenMapper_279_<String, GenModel_279_>() {
    override fun map(input: String): GenModel_279_ {
        val parts = input.split(":")
        return GenModel_279_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_279_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_279_,
    private val saveUseCase: GenSaveUseCase_279_,
    private val deleteUseCase: GenDeleteUseCase_279_,
    private val searchUseCase: GenSearchUseCase_279_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_279_>(GenState_279_.Idle)
    val state: StateFlow<GenState_279_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_279_) {
        when (event) {
            is GenEvent_279_.Load -> loadAll()
            is GenEvent_279_.Update -> save(event.model)
            is GenEvent_279_.Delete -> delete(event.id)
            is GenEvent_279_.Refresh -> loadAll()
            is GenEvent_279_.Search -> search(event.query)
            is GenEvent_279_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_279_.Loading; _state.value = GenState_279_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_279_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_279_.Success(searchUseCase(query)) } }
}
