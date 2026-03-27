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

data class GenModel_211_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_211_ {
    data class Load(val id: Long) : GenEvent_211_()
    data class Update(val model: GenModel_211_) : GenEvent_211_()
    data class Delete(val id: Long) : GenEvent_211_()
    data object Refresh : GenEvent_211_()
    data class Search(val query: String) : GenEvent_211_()
    data class Filter(val predicate: String) : GenEvent_211_()
}

sealed class GenState_211_ {
    data object Idle : GenState_211_()
    data object Loading : GenState_211_()
    data class Success(val items: List<GenModel_211_>) : GenState_211_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_211_()
    data class Partial(val items: List<GenModel_211_>, val hasMore: Boolean) : GenState_211_()
}

interface GenRepository_211_ {
    suspend fun getAll(): List<GenModel_211_>
    suspend fun getById(id: Long): GenModel_211_?
    suspend fun save(model: GenModel_211_): GenModel_211_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_211_>
}

@Singleton
class GenRepositoryImpl_211_ @Inject constructor() : GenRepository_211_ {
    private val store = mutableMapOf<Long, GenModel_211_>()
    override suspend fun getAll(): List<GenModel_211_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_211_? = store[id]
    override suspend fun save(model: GenModel_211_): GenModel_211_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_211_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_211_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_211_ @Inject constructor(
    private val repository: GenRepositoryImpl_211_
) : GenUseCase_211_<Unit, List<GenModel_211_>> {
    override suspend fun invoke(params: Unit): List<GenModel_211_> = repository.getAll()
}

class GenSaveUseCase_211_ @Inject constructor(
    private val repository: GenRepositoryImpl_211_
) : GenUseCase_211_<GenModel_211_, GenModel_211_> {
    override suspend fun invoke(params: GenModel_211_): GenModel_211_ = repository.save(params)
}

class GenDeleteUseCase_211_ @Inject constructor(
    private val repository: GenRepositoryImpl_211_
) : GenUseCase_211_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_211_ @Inject constructor(
    private val repository: GenRepositoryImpl_211_
) : GenUseCase_211_<String, List<GenModel_211_>> {
    override suspend fun invoke(params: String): List<GenModel_211_> = repository.search(params)
}

abstract class GenMapper_211_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_211_ : GenMapper_211_<GenModel_211_, String>() {
    override fun map(input: GenModel_211_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_211_ : GenMapper_211_<String, GenModel_211_>() {
    override fun map(input: String): GenModel_211_ {
        val parts = input.split(":")
        return GenModel_211_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_211_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_211_,
    private val saveUseCase: GenSaveUseCase_211_,
    private val deleteUseCase: GenDeleteUseCase_211_,
    private val searchUseCase: GenSearchUseCase_211_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_211_>(GenState_211_.Idle)
    val state: StateFlow<GenState_211_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_211_) {
        when (event) {
            is GenEvent_211_.Load -> loadAll()
            is GenEvent_211_.Update -> save(event.model)
            is GenEvent_211_.Delete -> delete(event.id)
            is GenEvent_211_.Refresh -> loadAll()
            is GenEvent_211_.Search -> search(event.query)
            is GenEvent_211_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_211_.Loading; _state.value = GenState_211_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_211_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_211_.Success(searchUseCase(query)) } }
}
