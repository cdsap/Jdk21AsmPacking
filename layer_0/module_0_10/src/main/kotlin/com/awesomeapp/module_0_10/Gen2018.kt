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

data class GenModel_2018_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2018_ {
    data class Load(val id: Long) : GenEvent_2018_()
    data class Update(val model: GenModel_2018_) : GenEvent_2018_()
    data class Delete(val id: Long) : GenEvent_2018_()
    data object Refresh : GenEvent_2018_()
    data class Search(val query: String) : GenEvent_2018_()
    data class Filter(val predicate: String) : GenEvent_2018_()
}

sealed class GenState_2018_ {
    data object Idle : GenState_2018_()
    data object Loading : GenState_2018_()
    data class Success(val items: List<GenModel_2018_>) : GenState_2018_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2018_()
    data class Partial(val items: List<GenModel_2018_>, val hasMore: Boolean) : GenState_2018_()
}

interface GenRepository_2018_ {
    suspend fun getAll(): List<GenModel_2018_>
    suspend fun getById(id: Long): GenModel_2018_?
    suspend fun save(model: GenModel_2018_): GenModel_2018_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2018_>
}

@Singleton
class GenRepositoryImpl_2018_ @Inject constructor() : GenRepository_2018_ {
    private val store = mutableMapOf<Long, GenModel_2018_>()
    override suspend fun getAll(): List<GenModel_2018_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2018_? = store[id]
    override suspend fun save(model: GenModel_2018_): GenModel_2018_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2018_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2018_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2018_ @Inject constructor(
    private val repository: GenRepositoryImpl_2018_
) : GenUseCase_2018_<Unit, List<GenModel_2018_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2018_> = repository.getAll()
}

class GenSaveUseCase_2018_ @Inject constructor(
    private val repository: GenRepositoryImpl_2018_
) : GenUseCase_2018_<GenModel_2018_, GenModel_2018_> {
    override suspend fun invoke(params: GenModel_2018_): GenModel_2018_ = repository.save(params)
}

class GenDeleteUseCase_2018_ @Inject constructor(
    private val repository: GenRepositoryImpl_2018_
) : GenUseCase_2018_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2018_ @Inject constructor(
    private val repository: GenRepositoryImpl_2018_
) : GenUseCase_2018_<String, List<GenModel_2018_>> {
    override suspend fun invoke(params: String): List<GenModel_2018_> = repository.search(params)
}

abstract class GenMapper_2018_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2018_ : GenMapper_2018_<GenModel_2018_, String>() {
    override fun map(input: GenModel_2018_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2018_ : GenMapper_2018_<String, GenModel_2018_>() {
    override fun map(input: String): GenModel_2018_ {
        val parts = input.split(":")
        return GenModel_2018_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2018_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2018_,
    private val saveUseCase: GenSaveUseCase_2018_,
    private val deleteUseCase: GenDeleteUseCase_2018_,
    private val searchUseCase: GenSearchUseCase_2018_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2018_>(GenState_2018_.Idle)
    val state: StateFlow<GenState_2018_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2018_) {
        when (event) {
            is GenEvent_2018_.Load -> loadAll()
            is GenEvent_2018_.Update -> save(event.model)
            is GenEvent_2018_.Delete -> delete(event.id)
            is GenEvent_2018_.Refresh -> loadAll()
            is GenEvent_2018_.Search -> search(event.query)
            is GenEvent_2018_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2018_.Loading; _state.value = GenState_2018_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2018_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2018_.Success(searchUseCase(query)) } }
}
