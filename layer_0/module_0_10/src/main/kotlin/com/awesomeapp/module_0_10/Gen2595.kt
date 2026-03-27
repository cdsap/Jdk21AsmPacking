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

data class GenModel_2595_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2595_ {
    data class Load(val id: Long) : GenEvent_2595_()
    data class Update(val model: GenModel_2595_) : GenEvent_2595_()
    data class Delete(val id: Long) : GenEvent_2595_()
    data object Refresh : GenEvent_2595_()
    data class Search(val query: String) : GenEvent_2595_()
    data class Filter(val predicate: String) : GenEvent_2595_()
}

sealed class GenState_2595_ {
    data object Idle : GenState_2595_()
    data object Loading : GenState_2595_()
    data class Success(val items: List<GenModel_2595_>) : GenState_2595_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2595_()
    data class Partial(val items: List<GenModel_2595_>, val hasMore: Boolean) : GenState_2595_()
}

interface GenRepository_2595_ {
    suspend fun getAll(): List<GenModel_2595_>
    suspend fun getById(id: Long): GenModel_2595_?
    suspend fun save(model: GenModel_2595_): GenModel_2595_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2595_>
}

@Singleton
class GenRepositoryImpl_2595_ @Inject constructor() : GenRepository_2595_ {
    private val store = mutableMapOf<Long, GenModel_2595_>()
    override suspend fun getAll(): List<GenModel_2595_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2595_? = store[id]
    override suspend fun save(model: GenModel_2595_): GenModel_2595_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2595_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2595_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2595_ @Inject constructor(
    private val repository: GenRepositoryImpl_2595_
) : GenUseCase_2595_<Unit, List<GenModel_2595_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2595_> = repository.getAll()
}

class GenSaveUseCase_2595_ @Inject constructor(
    private val repository: GenRepositoryImpl_2595_
) : GenUseCase_2595_<GenModel_2595_, GenModel_2595_> {
    override suspend fun invoke(params: GenModel_2595_): GenModel_2595_ = repository.save(params)
}

class GenDeleteUseCase_2595_ @Inject constructor(
    private val repository: GenRepositoryImpl_2595_
) : GenUseCase_2595_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2595_ @Inject constructor(
    private val repository: GenRepositoryImpl_2595_
) : GenUseCase_2595_<String, List<GenModel_2595_>> {
    override suspend fun invoke(params: String): List<GenModel_2595_> = repository.search(params)
}

abstract class GenMapper_2595_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2595_ : GenMapper_2595_<GenModel_2595_, String>() {
    override fun map(input: GenModel_2595_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2595_ : GenMapper_2595_<String, GenModel_2595_>() {
    override fun map(input: String): GenModel_2595_ {
        val parts = input.split(":")
        return GenModel_2595_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2595_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2595_,
    private val saveUseCase: GenSaveUseCase_2595_,
    private val deleteUseCase: GenDeleteUseCase_2595_,
    private val searchUseCase: GenSearchUseCase_2595_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2595_>(GenState_2595_.Idle)
    val state: StateFlow<GenState_2595_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2595_) {
        when (event) {
            is GenEvent_2595_.Load -> loadAll()
            is GenEvent_2595_.Update -> save(event.model)
            is GenEvent_2595_.Delete -> delete(event.id)
            is GenEvent_2595_.Refresh -> loadAll()
            is GenEvent_2595_.Search -> search(event.query)
            is GenEvent_2595_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2595_.Loading; _state.value = GenState_2595_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2595_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2595_.Success(searchUseCase(query)) } }
}
