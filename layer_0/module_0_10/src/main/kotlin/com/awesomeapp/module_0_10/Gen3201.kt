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

data class GenModel_3201_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3201_ {
    data class Load(val id: Long) : GenEvent_3201_()
    data class Update(val model: GenModel_3201_) : GenEvent_3201_()
    data class Delete(val id: Long) : GenEvent_3201_()
    data object Refresh : GenEvent_3201_()
    data class Search(val query: String) : GenEvent_3201_()
    data class Filter(val predicate: String) : GenEvent_3201_()
}

sealed class GenState_3201_ {
    data object Idle : GenState_3201_()
    data object Loading : GenState_3201_()
    data class Success(val items: List<GenModel_3201_>) : GenState_3201_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3201_()
    data class Partial(val items: List<GenModel_3201_>, val hasMore: Boolean) : GenState_3201_()
}

interface GenRepository_3201_ {
    suspend fun getAll(): List<GenModel_3201_>
    suspend fun getById(id: Long): GenModel_3201_?
    suspend fun save(model: GenModel_3201_): GenModel_3201_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3201_>
}

@Singleton
class GenRepositoryImpl_3201_ @Inject constructor() : GenRepository_3201_ {
    private val store = mutableMapOf<Long, GenModel_3201_>()
    override suspend fun getAll(): List<GenModel_3201_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3201_? = store[id]
    override suspend fun save(model: GenModel_3201_): GenModel_3201_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3201_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3201_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3201_ @Inject constructor(
    private val repository: GenRepositoryImpl_3201_
) : GenUseCase_3201_<Unit, List<GenModel_3201_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3201_> = repository.getAll()
}

class GenSaveUseCase_3201_ @Inject constructor(
    private val repository: GenRepositoryImpl_3201_
) : GenUseCase_3201_<GenModel_3201_, GenModel_3201_> {
    override suspend fun invoke(params: GenModel_3201_): GenModel_3201_ = repository.save(params)
}

class GenDeleteUseCase_3201_ @Inject constructor(
    private val repository: GenRepositoryImpl_3201_
) : GenUseCase_3201_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3201_ @Inject constructor(
    private val repository: GenRepositoryImpl_3201_
) : GenUseCase_3201_<String, List<GenModel_3201_>> {
    override suspend fun invoke(params: String): List<GenModel_3201_> = repository.search(params)
}

abstract class GenMapper_3201_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3201_ : GenMapper_3201_<GenModel_3201_, String>() {
    override fun map(input: GenModel_3201_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3201_ : GenMapper_3201_<String, GenModel_3201_>() {
    override fun map(input: String): GenModel_3201_ {
        val parts = input.split(":")
        return GenModel_3201_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3201_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3201_,
    private val saveUseCase: GenSaveUseCase_3201_,
    private val deleteUseCase: GenDeleteUseCase_3201_,
    private val searchUseCase: GenSearchUseCase_3201_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3201_>(GenState_3201_.Idle)
    val state: StateFlow<GenState_3201_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3201_) {
        when (event) {
            is GenEvent_3201_.Load -> loadAll()
            is GenEvent_3201_.Update -> save(event.model)
            is GenEvent_3201_.Delete -> delete(event.id)
            is GenEvent_3201_.Refresh -> loadAll()
            is GenEvent_3201_.Search -> search(event.query)
            is GenEvent_3201_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3201_.Loading; _state.value = GenState_3201_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3201_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3201_.Success(searchUseCase(query)) } }
}
