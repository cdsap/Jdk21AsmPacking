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

data class GenModel_281_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_281_ {
    data class Load(val id: Long) : GenEvent_281_()
    data class Update(val model: GenModel_281_) : GenEvent_281_()
    data class Delete(val id: Long) : GenEvent_281_()
    data object Refresh : GenEvent_281_()
    data class Search(val query: String) : GenEvent_281_()
    data class Filter(val predicate: String) : GenEvent_281_()
}

sealed class GenState_281_ {
    data object Idle : GenState_281_()
    data object Loading : GenState_281_()
    data class Success(val items: List<GenModel_281_>) : GenState_281_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_281_()
    data class Partial(val items: List<GenModel_281_>, val hasMore: Boolean) : GenState_281_()
}

interface GenRepository_281_ {
    suspend fun getAll(): List<GenModel_281_>
    suspend fun getById(id: Long): GenModel_281_?
    suspend fun save(model: GenModel_281_): GenModel_281_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_281_>
}

@Singleton
class GenRepositoryImpl_281_ @Inject constructor() : GenRepository_281_ {
    private val store = mutableMapOf<Long, GenModel_281_>()
    override suspend fun getAll(): List<GenModel_281_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_281_? = store[id]
    override suspend fun save(model: GenModel_281_): GenModel_281_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_281_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_281_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_281_ @Inject constructor(
    private val repository: GenRepositoryImpl_281_
) : GenUseCase_281_<Unit, List<GenModel_281_>> {
    override suspend fun invoke(params: Unit): List<GenModel_281_> = repository.getAll()
}

class GenSaveUseCase_281_ @Inject constructor(
    private val repository: GenRepositoryImpl_281_
) : GenUseCase_281_<GenModel_281_, GenModel_281_> {
    override suspend fun invoke(params: GenModel_281_): GenModel_281_ = repository.save(params)
}

class GenDeleteUseCase_281_ @Inject constructor(
    private val repository: GenRepositoryImpl_281_
) : GenUseCase_281_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_281_ @Inject constructor(
    private val repository: GenRepositoryImpl_281_
) : GenUseCase_281_<String, List<GenModel_281_>> {
    override suspend fun invoke(params: String): List<GenModel_281_> = repository.search(params)
}

abstract class GenMapper_281_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_281_ : GenMapper_281_<GenModel_281_, String>() {
    override fun map(input: GenModel_281_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_281_ : GenMapper_281_<String, GenModel_281_>() {
    override fun map(input: String): GenModel_281_ {
        val parts = input.split(":")
        return GenModel_281_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_281_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_281_,
    private val saveUseCase: GenSaveUseCase_281_,
    private val deleteUseCase: GenDeleteUseCase_281_,
    private val searchUseCase: GenSearchUseCase_281_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_281_>(GenState_281_.Idle)
    val state: StateFlow<GenState_281_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_281_) {
        when (event) {
            is GenEvent_281_.Load -> loadAll()
            is GenEvent_281_.Update -> save(event.model)
            is GenEvent_281_.Delete -> delete(event.id)
            is GenEvent_281_.Refresh -> loadAll()
            is GenEvent_281_.Search -> search(event.query)
            is GenEvent_281_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_281_.Loading; _state.value = GenState_281_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_281_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_281_.Success(searchUseCase(query)) } }
}
