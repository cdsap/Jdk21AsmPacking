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

data class GenModel_2567_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2567_ {
    data class Load(val id: Long) : GenEvent_2567_()
    data class Update(val model: GenModel_2567_) : GenEvent_2567_()
    data class Delete(val id: Long) : GenEvent_2567_()
    data object Refresh : GenEvent_2567_()
    data class Search(val query: String) : GenEvent_2567_()
    data class Filter(val predicate: String) : GenEvent_2567_()
}

sealed class GenState_2567_ {
    data object Idle : GenState_2567_()
    data object Loading : GenState_2567_()
    data class Success(val items: List<GenModel_2567_>) : GenState_2567_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2567_()
    data class Partial(val items: List<GenModel_2567_>, val hasMore: Boolean) : GenState_2567_()
}

interface GenRepository_2567_ {
    suspend fun getAll(): List<GenModel_2567_>
    suspend fun getById(id: Long): GenModel_2567_?
    suspend fun save(model: GenModel_2567_): GenModel_2567_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2567_>
}

@Singleton
class GenRepositoryImpl_2567_ @Inject constructor() : GenRepository_2567_ {
    private val store = mutableMapOf<Long, GenModel_2567_>()
    override suspend fun getAll(): List<GenModel_2567_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2567_? = store[id]
    override suspend fun save(model: GenModel_2567_): GenModel_2567_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2567_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2567_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2567_ @Inject constructor(
    private val repository: GenRepositoryImpl_2567_
) : GenUseCase_2567_<Unit, List<GenModel_2567_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2567_> = repository.getAll()
}

class GenSaveUseCase_2567_ @Inject constructor(
    private val repository: GenRepositoryImpl_2567_
) : GenUseCase_2567_<GenModel_2567_, GenModel_2567_> {
    override suspend fun invoke(params: GenModel_2567_): GenModel_2567_ = repository.save(params)
}

class GenDeleteUseCase_2567_ @Inject constructor(
    private val repository: GenRepositoryImpl_2567_
) : GenUseCase_2567_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2567_ @Inject constructor(
    private val repository: GenRepositoryImpl_2567_
) : GenUseCase_2567_<String, List<GenModel_2567_>> {
    override suspend fun invoke(params: String): List<GenModel_2567_> = repository.search(params)
}

abstract class GenMapper_2567_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2567_ : GenMapper_2567_<GenModel_2567_, String>() {
    override fun map(input: GenModel_2567_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2567_ : GenMapper_2567_<String, GenModel_2567_>() {
    override fun map(input: String): GenModel_2567_ {
        val parts = input.split(":")
        return GenModel_2567_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2567_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2567_,
    private val saveUseCase: GenSaveUseCase_2567_,
    private val deleteUseCase: GenDeleteUseCase_2567_,
    private val searchUseCase: GenSearchUseCase_2567_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2567_>(GenState_2567_.Idle)
    val state: StateFlow<GenState_2567_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2567_) {
        when (event) {
            is GenEvent_2567_.Load -> loadAll()
            is GenEvent_2567_.Update -> save(event.model)
            is GenEvent_2567_.Delete -> delete(event.id)
            is GenEvent_2567_.Refresh -> loadAll()
            is GenEvent_2567_.Search -> search(event.query)
            is GenEvent_2567_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2567_.Loading; _state.value = GenState_2567_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2567_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2567_.Success(searchUseCase(query)) } }
}
