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

data class GenModel_595_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_595_ {
    data class Load(val id: Long) : GenEvent_595_()
    data class Update(val model: GenModel_595_) : GenEvent_595_()
    data class Delete(val id: Long) : GenEvent_595_()
    data object Refresh : GenEvent_595_()
    data class Search(val query: String) : GenEvent_595_()
    data class Filter(val predicate: String) : GenEvent_595_()
}

sealed class GenState_595_ {
    data object Idle : GenState_595_()
    data object Loading : GenState_595_()
    data class Success(val items: List<GenModel_595_>) : GenState_595_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_595_()
    data class Partial(val items: List<GenModel_595_>, val hasMore: Boolean) : GenState_595_()
}

interface GenRepository_595_ {
    suspend fun getAll(): List<GenModel_595_>
    suspend fun getById(id: Long): GenModel_595_?
    suspend fun save(model: GenModel_595_): GenModel_595_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_595_>
}

@Singleton
class GenRepositoryImpl_595_ @Inject constructor() : GenRepository_595_ {
    private val store = mutableMapOf<Long, GenModel_595_>()
    override suspend fun getAll(): List<GenModel_595_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_595_? = store[id]
    override suspend fun save(model: GenModel_595_): GenModel_595_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_595_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_595_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_595_ @Inject constructor(
    private val repository: GenRepositoryImpl_595_
) : GenUseCase_595_<Unit, List<GenModel_595_>> {
    override suspend fun invoke(params: Unit): List<GenModel_595_> = repository.getAll()
}

class GenSaveUseCase_595_ @Inject constructor(
    private val repository: GenRepositoryImpl_595_
) : GenUseCase_595_<GenModel_595_, GenModel_595_> {
    override suspend fun invoke(params: GenModel_595_): GenModel_595_ = repository.save(params)
}

class GenDeleteUseCase_595_ @Inject constructor(
    private val repository: GenRepositoryImpl_595_
) : GenUseCase_595_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_595_ @Inject constructor(
    private val repository: GenRepositoryImpl_595_
) : GenUseCase_595_<String, List<GenModel_595_>> {
    override suspend fun invoke(params: String): List<GenModel_595_> = repository.search(params)
}

abstract class GenMapper_595_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_595_ : GenMapper_595_<GenModel_595_, String>() {
    override fun map(input: GenModel_595_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_595_ : GenMapper_595_<String, GenModel_595_>() {
    override fun map(input: String): GenModel_595_ {
        val parts = input.split(":")
        return GenModel_595_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_595_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_595_,
    private val saveUseCase: GenSaveUseCase_595_,
    private val deleteUseCase: GenDeleteUseCase_595_,
    private val searchUseCase: GenSearchUseCase_595_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_595_>(GenState_595_.Idle)
    val state: StateFlow<GenState_595_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_595_) {
        when (event) {
            is GenEvent_595_.Load -> loadAll()
            is GenEvent_595_.Update -> save(event.model)
            is GenEvent_595_.Delete -> delete(event.id)
            is GenEvent_595_.Refresh -> loadAll()
            is GenEvent_595_.Search -> search(event.query)
            is GenEvent_595_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_595_.Loading; _state.value = GenState_595_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_595_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_595_.Success(searchUseCase(query)) } }
}
