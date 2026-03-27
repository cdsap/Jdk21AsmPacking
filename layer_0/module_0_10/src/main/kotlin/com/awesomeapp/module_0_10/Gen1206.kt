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

data class GenModel_1206_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1206_ {
    data class Load(val id: Long) : GenEvent_1206_()
    data class Update(val model: GenModel_1206_) : GenEvent_1206_()
    data class Delete(val id: Long) : GenEvent_1206_()
    data object Refresh : GenEvent_1206_()
    data class Search(val query: String) : GenEvent_1206_()
    data class Filter(val predicate: String) : GenEvent_1206_()
}

sealed class GenState_1206_ {
    data object Idle : GenState_1206_()
    data object Loading : GenState_1206_()
    data class Success(val items: List<GenModel_1206_>) : GenState_1206_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1206_()
    data class Partial(val items: List<GenModel_1206_>, val hasMore: Boolean) : GenState_1206_()
}

interface GenRepository_1206_ {
    suspend fun getAll(): List<GenModel_1206_>
    suspend fun getById(id: Long): GenModel_1206_?
    suspend fun save(model: GenModel_1206_): GenModel_1206_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1206_>
}

@Singleton
class GenRepositoryImpl_1206_ @Inject constructor() : GenRepository_1206_ {
    private val store = mutableMapOf<Long, GenModel_1206_>()
    override suspend fun getAll(): List<GenModel_1206_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1206_? = store[id]
    override suspend fun save(model: GenModel_1206_): GenModel_1206_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1206_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1206_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1206_ @Inject constructor(
    private val repository: GenRepositoryImpl_1206_
) : GenUseCase_1206_<Unit, List<GenModel_1206_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1206_> = repository.getAll()
}

class GenSaveUseCase_1206_ @Inject constructor(
    private val repository: GenRepositoryImpl_1206_
) : GenUseCase_1206_<GenModel_1206_, GenModel_1206_> {
    override suspend fun invoke(params: GenModel_1206_): GenModel_1206_ = repository.save(params)
}

class GenDeleteUseCase_1206_ @Inject constructor(
    private val repository: GenRepositoryImpl_1206_
) : GenUseCase_1206_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1206_ @Inject constructor(
    private val repository: GenRepositoryImpl_1206_
) : GenUseCase_1206_<String, List<GenModel_1206_>> {
    override suspend fun invoke(params: String): List<GenModel_1206_> = repository.search(params)
}

abstract class GenMapper_1206_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1206_ : GenMapper_1206_<GenModel_1206_, String>() {
    override fun map(input: GenModel_1206_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1206_ : GenMapper_1206_<String, GenModel_1206_>() {
    override fun map(input: String): GenModel_1206_ {
        val parts = input.split(":")
        return GenModel_1206_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1206_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1206_,
    private val saveUseCase: GenSaveUseCase_1206_,
    private val deleteUseCase: GenDeleteUseCase_1206_,
    private val searchUseCase: GenSearchUseCase_1206_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1206_>(GenState_1206_.Idle)
    val state: StateFlow<GenState_1206_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1206_) {
        when (event) {
            is GenEvent_1206_.Load -> loadAll()
            is GenEvent_1206_.Update -> save(event.model)
            is GenEvent_1206_.Delete -> delete(event.id)
            is GenEvent_1206_.Refresh -> loadAll()
            is GenEvent_1206_.Search -> search(event.query)
            is GenEvent_1206_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1206_.Loading; _state.value = GenState_1206_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1206_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1206_.Success(searchUseCase(query)) } }
}
