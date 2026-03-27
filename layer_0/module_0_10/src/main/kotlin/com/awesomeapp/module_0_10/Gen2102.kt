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

data class GenModel_2102_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2102_ {
    data class Load(val id: Long) : GenEvent_2102_()
    data class Update(val model: GenModel_2102_) : GenEvent_2102_()
    data class Delete(val id: Long) : GenEvent_2102_()
    data object Refresh : GenEvent_2102_()
    data class Search(val query: String) : GenEvent_2102_()
    data class Filter(val predicate: String) : GenEvent_2102_()
}

sealed class GenState_2102_ {
    data object Idle : GenState_2102_()
    data object Loading : GenState_2102_()
    data class Success(val items: List<GenModel_2102_>) : GenState_2102_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2102_()
    data class Partial(val items: List<GenModel_2102_>, val hasMore: Boolean) : GenState_2102_()
}

interface GenRepository_2102_ {
    suspend fun getAll(): List<GenModel_2102_>
    suspend fun getById(id: Long): GenModel_2102_?
    suspend fun save(model: GenModel_2102_): GenModel_2102_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2102_>
}

@Singleton
class GenRepositoryImpl_2102_ @Inject constructor() : GenRepository_2102_ {
    private val store = mutableMapOf<Long, GenModel_2102_>()
    override suspend fun getAll(): List<GenModel_2102_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2102_? = store[id]
    override suspend fun save(model: GenModel_2102_): GenModel_2102_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2102_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2102_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2102_ @Inject constructor(
    private val repository: GenRepositoryImpl_2102_
) : GenUseCase_2102_<Unit, List<GenModel_2102_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2102_> = repository.getAll()
}

class GenSaveUseCase_2102_ @Inject constructor(
    private val repository: GenRepositoryImpl_2102_
) : GenUseCase_2102_<GenModel_2102_, GenModel_2102_> {
    override suspend fun invoke(params: GenModel_2102_): GenModel_2102_ = repository.save(params)
}

class GenDeleteUseCase_2102_ @Inject constructor(
    private val repository: GenRepositoryImpl_2102_
) : GenUseCase_2102_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2102_ @Inject constructor(
    private val repository: GenRepositoryImpl_2102_
) : GenUseCase_2102_<String, List<GenModel_2102_>> {
    override suspend fun invoke(params: String): List<GenModel_2102_> = repository.search(params)
}

abstract class GenMapper_2102_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2102_ : GenMapper_2102_<GenModel_2102_, String>() {
    override fun map(input: GenModel_2102_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2102_ : GenMapper_2102_<String, GenModel_2102_>() {
    override fun map(input: String): GenModel_2102_ {
        val parts = input.split(":")
        return GenModel_2102_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2102_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2102_,
    private val saveUseCase: GenSaveUseCase_2102_,
    private val deleteUseCase: GenDeleteUseCase_2102_,
    private val searchUseCase: GenSearchUseCase_2102_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2102_>(GenState_2102_.Idle)
    val state: StateFlow<GenState_2102_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2102_) {
        when (event) {
            is GenEvent_2102_.Load -> loadAll()
            is GenEvent_2102_.Update -> save(event.model)
            is GenEvent_2102_.Delete -> delete(event.id)
            is GenEvent_2102_.Refresh -> loadAll()
            is GenEvent_2102_.Search -> search(event.query)
            is GenEvent_2102_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2102_.Loading; _state.value = GenState_2102_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2102_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2102_.Success(searchUseCase(query)) } }
}
