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

data class GenModel_808_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_808_ {
    data class Load(val id: Long) : GenEvent_808_()
    data class Update(val model: GenModel_808_) : GenEvent_808_()
    data class Delete(val id: Long) : GenEvent_808_()
    data object Refresh : GenEvent_808_()
    data class Search(val query: String) : GenEvent_808_()
    data class Filter(val predicate: String) : GenEvent_808_()
}

sealed class GenState_808_ {
    data object Idle : GenState_808_()
    data object Loading : GenState_808_()
    data class Success(val items: List<GenModel_808_>) : GenState_808_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_808_()
    data class Partial(val items: List<GenModel_808_>, val hasMore: Boolean) : GenState_808_()
}

interface GenRepository_808_ {
    suspend fun getAll(): List<GenModel_808_>
    suspend fun getById(id: Long): GenModel_808_?
    suspend fun save(model: GenModel_808_): GenModel_808_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_808_>
}

@Singleton
class GenRepositoryImpl_808_ @Inject constructor() : GenRepository_808_ {
    private val store = mutableMapOf<Long, GenModel_808_>()
    override suspend fun getAll(): List<GenModel_808_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_808_? = store[id]
    override suspend fun save(model: GenModel_808_): GenModel_808_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_808_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_808_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_808_ @Inject constructor(
    private val repository: GenRepositoryImpl_808_
) : GenUseCase_808_<Unit, List<GenModel_808_>> {
    override suspend fun invoke(params: Unit): List<GenModel_808_> = repository.getAll()
}

class GenSaveUseCase_808_ @Inject constructor(
    private val repository: GenRepositoryImpl_808_
) : GenUseCase_808_<GenModel_808_, GenModel_808_> {
    override suspend fun invoke(params: GenModel_808_): GenModel_808_ = repository.save(params)
}

class GenDeleteUseCase_808_ @Inject constructor(
    private val repository: GenRepositoryImpl_808_
) : GenUseCase_808_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_808_ @Inject constructor(
    private val repository: GenRepositoryImpl_808_
) : GenUseCase_808_<String, List<GenModel_808_>> {
    override suspend fun invoke(params: String): List<GenModel_808_> = repository.search(params)
}

abstract class GenMapper_808_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_808_ : GenMapper_808_<GenModel_808_, String>() {
    override fun map(input: GenModel_808_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_808_ : GenMapper_808_<String, GenModel_808_>() {
    override fun map(input: String): GenModel_808_ {
        val parts = input.split(":")
        return GenModel_808_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_808_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_808_,
    private val saveUseCase: GenSaveUseCase_808_,
    private val deleteUseCase: GenDeleteUseCase_808_,
    private val searchUseCase: GenSearchUseCase_808_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_808_>(GenState_808_.Idle)
    val state: StateFlow<GenState_808_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_808_) {
        when (event) {
            is GenEvent_808_.Load -> loadAll()
            is GenEvent_808_.Update -> save(event.model)
            is GenEvent_808_.Delete -> delete(event.id)
            is GenEvent_808_.Refresh -> loadAll()
            is GenEvent_808_.Search -> search(event.query)
            is GenEvent_808_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_808_.Loading; _state.value = GenState_808_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_808_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_808_.Success(searchUseCase(query)) } }
}
