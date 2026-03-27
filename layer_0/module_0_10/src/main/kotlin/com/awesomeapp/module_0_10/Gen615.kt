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

data class GenModel_615_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_615_ {
    data class Load(val id: Long) : GenEvent_615_()
    data class Update(val model: GenModel_615_) : GenEvent_615_()
    data class Delete(val id: Long) : GenEvent_615_()
    data object Refresh : GenEvent_615_()
    data class Search(val query: String) : GenEvent_615_()
    data class Filter(val predicate: String) : GenEvent_615_()
}

sealed class GenState_615_ {
    data object Idle : GenState_615_()
    data object Loading : GenState_615_()
    data class Success(val items: List<GenModel_615_>) : GenState_615_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_615_()
    data class Partial(val items: List<GenModel_615_>, val hasMore: Boolean) : GenState_615_()
}

interface GenRepository_615_ {
    suspend fun getAll(): List<GenModel_615_>
    suspend fun getById(id: Long): GenModel_615_?
    suspend fun save(model: GenModel_615_): GenModel_615_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_615_>
}

@Singleton
class GenRepositoryImpl_615_ @Inject constructor() : GenRepository_615_ {
    private val store = mutableMapOf<Long, GenModel_615_>()
    override suspend fun getAll(): List<GenModel_615_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_615_? = store[id]
    override suspend fun save(model: GenModel_615_): GenModel_615_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_615_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_615_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_615_ @Inject constructor(
    private val repository: GenRepositoryImpl_615_
) : GenUseCase_615_<Unit, List<GenModel_615_>> {
    override suspend fun invoke(params: Unit): List<GenModel_615_> = repository.getAll()
}

class GenSaveUseCase_615_ @Inject constructor(
    private val repository: GenRepositoryImpl_615_
) : GenUseCase_615_<GenModel_615_, GenModel_615_> {
    override suspend fun invoke(params: GenModel_615_): GenModel_615_ = repository.save(params)
}

class GenDeleteUseCase_615_ @Inject constructor(
    private val repository: GenRepositoryImpl_615_
) : GenUseCase_615_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_615_ @Inject constructor(
    private val repository: GenRepositoryImpl_615_
) : GenUseCase_615_<String, List<GenModel_615_>> {
    override suspend fun invoke(params: String): List<GenModel_615_> = repository.search(params)
}

abstract class GenMapper_615_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_615_ : GenMapper_615_<GenModel_615_, String>() {
    override fun map(input: GenModel_615_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_615_ : GenMapper_615_<String, GenModel_615_>() {
    override fun map(input: String): GenModel_615_ {
        val parts = input.split(":")
        return GenModel_615_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_615_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_615_,
    private val saveUseCase: GenSaveUseCase_615_,
    private val deleteUseCase: GenDeleteUseCase_615_,
    private val searchUseCase: GenSearchUseCase_615_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_615_>(GenState_615_.Idle)
    val state: StateFlow<GenState_615_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_615_) {
        when (event) {
            is GenEvent_615_.Load -> loadAll()
            is GenEvent_615_.Update -> save(event.model)
            is GenEvent_615_.Delete -> delete(event.id)
            is GenEvent_615_.Refresh -> loadAll()
            is GenEvent_615_.Search -> search(event.query)
            is GenEvent_615_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_615_.Loading; _state.value = GenState_615_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_615_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_615_.Success(searchUseCase(query)) } }
}
