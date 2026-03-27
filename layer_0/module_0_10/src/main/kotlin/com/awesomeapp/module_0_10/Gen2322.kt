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

data class GenModel_2322_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2322_ {
    data class Load(val id: Long) : GenEvent_2322_()
    data class Update(val model: GenModel_2322_) : GenEvent_2322_()
    data class Delete(val id: Long) : GenEvent_2322_()
    data object Refresh : GenEvent_2322_()
    data class Search(val query: String) : GenEvent_2322_()
    data class Filter(val predicate: String) : GenEvent_2322_()
}

sealed class GenState_2322_ {
    data object Idle : GenState_2322_()
    data object Loading : GenState_2322_()
    data class Success(val items: List<GenModel_2322_>) : GenState_2322_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2322_()
    data class Partial(val items: List<GenModel_2322_>, val hasMore: Boolean) : GenState_2322_()
}

interface GenRepository_2322_ {
    suspend fun getAll(): List<GenModel_2322_>
    suspend fun getById(id: Long): GenModel_2322_?
    suspend fun save(model: GenModel_2322_): GenModel_2322_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2322_>
}

@Singleton
class GenRepositoryImpl_2322_ @Inject constructor() : GenRepository_2322_ {
    private val store = mutableMapOf<Long, GenModel_2322_>()
    override suspend fun getAll(): List<GenModel_2322_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2322_? = store[id]
    override suspend fun save(model: GenModel_2322_): GenModel_2322_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2322_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2322_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2322_ @Inject constructor(
    private val repository: GenRepositoryImpl_2322_
) : GenUseCase_2322_<Unit, List<GenModel_2322_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2322_> = repository.getAll()
}

class GenSaveUseCase_2322_ @Inject constructor(
    private val repository: GenRepositoryImpl_2322_
) : GenUseCase_2322_<GenModel_2322_, GenModel_2322_> {
    override suspend fun invoke(params: GenModel_2322_): GenModel_2322_ = repository.save(params)
}

class GenDeleteUseCase_2322_ @Inject constructor(
    private val repository: GenRepositoryImpl_2322_
) : GenUseCase_2322_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2322_ @Inject constructor(
    private val repository: GenRepositoryImpl_2322_
) : GenUseCase_2322_<String, List<GenModel_2322_>> {
    override suspend fun invoke(params: String): List<GenModel_2322_> = repository.search(params)
}

abstract class GenMapper_2322_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2322_ : GenMapper_2322_<GenModel_2322_, String>() {
    override fun map(input: GenModel_2322_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2322_ : GenMapper_2322_<String, GenModel_2322_>() {
    override fun map(input: String): GenModel_2322_ {
        val parts = input.split(":")
        return GenModel_2322_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2322_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2322_,
    private val saveUseCase: GenSaveUseCase_2322_,
    private val deleteUseCase: GenDeleteUseCase_2322_,
    private val searchUseCase: GenSearchUseCase_2322_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2322_>(GenState_2322_.Idle)
    val state: StateFlow<GenState_2322_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2322_) {
        when (event) {
            is GenEvent_2322_.Load -> loadAll()
            is GenEvent_2322_.Update -> save(event.model)
            is GenEvent_2322_.Delete -> delete(event.id)
            is GenEvent_2322_.Refresh -> loadAll()
            is GenEvent_2322_.Search -> search(event.query)
            is GenEvent_2322_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2322_.Loading; _state.value = GenState_2322_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2322_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2322_.Success(searchUseCase(query)) } }
}
