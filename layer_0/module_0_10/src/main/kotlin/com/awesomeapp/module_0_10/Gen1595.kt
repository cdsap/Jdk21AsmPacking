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

data class GenModel_1595_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1595_ {
    data class Load(val id: Long) : GenEvent_1595_()
    data class Update(val model: GenModel_1595_) : GenEvent_1595_()
    data class Delete(val id: Long) : GenEvent_1595_()
    data object Refresh : GenEvent_1595_()
    data class Search(val query: String) : GenEvent_1595_()
    data class Filter(val predicate: String) : GenEvent_1595_()
}

sealed class GenState_1595_ {
    data object Idle : GenState_1595_()
    data object Loading : GenState_1595_()
    data class Success(val items: List<GenModel_1595_>) : GenState_1595_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1595_()
    data class Partial(val items: List<GenModel_1595_>, val hasMore: Boolean) : GenState_1595_()
}

interface GenRepository_1595_ {
    suspend fun getAll(): List<GenModel_1595_>
    suspend fun getById(id: Long): GenModel_1595_?
    suspend fun save(model: GenModel_1595_): GenModel_1595_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1595_>
}

@Singleton
class GenRepositoryImpl_1595_ @Inject constructor() : GenRepository_1595_ {
    private val store = mutableMapOf<Long, GenModel_1595_>()
    override suspend fun getAll(): List<GenModel_1595_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1595_? = store[id]
    override suspend fun save(model: GenModel_1595_): GenModel_1595_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1595_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1595_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1595_ @Inject constructor(
    private val repository: GenRepositoryImpl_1595_
) : GenUseCase_1595_<Unit, List<GenModel_1595_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1595_> = repository.getAll()
}

class GenSaveUseCase_1595_ @Inject constructor(
    private val repository: GenRepositoryImpl_1595_
) : GenUseCase_1595_<GenModel_1595_, GenModel_1595_> {
    override suspend fun invoke(params: GenModel_1595_): GenModel_1595_ = repository.save(params)
}

class GenDeleteUseCase_1595_ @Inject constructor(
    private val repository: GenRepositoryImpl_1595_
) : GenUseCase_1595_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1595_ @Inject constructor(
    private val repository: GenRepositoryImpl_1595_
) : GenUseCase_1595_<String, List<GenModel_1595_>> {
    override suspend fun invoke(params: String): List<GenModel_1595_> = repository.search(params)
}

abstract class GenMapper_1595_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1595_ : GenMapper_1595_<GenModel_1595_, String>() {
    override fun map(input: GenModel_1595_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1595_ : GenMapper_1595_<String, GenModel_1595_>() {
    override fun map(input: String): GenModel_1595_ {
        val parts = input.split(":")
        return GenModel_1595_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1595_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1595_,
    private val saveUseCase: GenSaveUseCase_1595_,
    private val deleteUseCase: GenDeleteUseCase_1595_,
    private val searchUseCase: GenSearchUseCase_1595_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1595_>(GenState_1595_.Idle)
    val state: StateFlow<GenState_1595_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1595_) {
        when (event) {
            is GenEvent_1595_.Load -> loadAll()
            is GenEvent_1595_.Update -> save(event.model)
            is GenEvent_1595_.Delete -> delete(event.id)
            is GenEvent_1595_.Refresh -> loadAll()
            is GenEvent_1595_.Search -> search(event.query)
            is GenEvent_1595_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1595_.Loading; _state.value = GenState_1595_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1595_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1595_.Success(searchUseCase(query)) } }
}
