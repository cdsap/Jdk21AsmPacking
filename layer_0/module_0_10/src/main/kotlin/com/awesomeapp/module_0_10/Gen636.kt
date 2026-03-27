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

data class GenModel_636_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_636_ {
    data class Load(val id: Long) : GenEvent_636_()
    data class Update(val model: GenModel_636_) : GenEvent_636_()
    data class Delete(val id: Long) : GenEvent_636_()
    data object Refresh : GenEvent_636_()
    data class Search(val query: String) : GenEvent_636_()
    data class Filter(val predicate: String) : GenEvent_636_()
}

sealed class GenState_636_ {
    data object Idle : GenState_636_()
    data object Loading : GenState_636_()
    data class Success(val items: List<GenModel_636_>) : GenState_636_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_636_()
    data class Partial(val items: List<GenModel_636_>, val hasMore: Boolean) : GenState_636_()
}

interface GenRepository_636_ {
    suspend fun getAll(): List<GenModel_636_>
    suspend fun getById(id: Long): GenModel_636_?
    suspend fun save(model: GenModel_636_): GenModel_636_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_636_>
}

@Singleton
class GenRepositoryImpl_636_ @Inject constructor() : GenRepository_636_ {
    private val store = mutableMapOf<Long, GenModel_636_>()
    override suspend fun getAll(): List<GenModel_636_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_636_? = store[id]
    override suspend fun save(model: GenModel_636_): GenModel_636_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_636_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_636_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_636_ @Inject constructor(
    private val repository: GenRepositoryImpl_636_
) : GenUseCase_636_<Unit, List<GenModel_636_>> {
    override suspend fun invoke(params: Unit): List<GenModel_636_> = repository.getAll()
}

class GenSaveUseCase_636_ @Inject constructor(
    private val repository: GenRepositoryImpl_636_
) : GenUseCase_636_<GenModel_636_, GenModel_636_> {
    override suspend fun invoke(params: GenModel_636_): GenModel_636_ = repository.save(params)
}

class GenDeleteUseCase_636_ @Inject constructor(
    private val repository: GenRepositoryImpl_636_
) : GenUseCase_636_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_636_ @Inject constructor(
    private val repository: GenRepositoryImpl_636_
) : GenUseCase_636_<String, List<GenModel_636_>> {
    override suspend fun invoke(params: String): List<GenModel_636_> = repository.search(params)
}

abstract class GenMapper_636_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_636_ : GenMapper_636_<GenModel_636_, String>() {
    override fun map(input: GenModel_636_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_636_ : GenMapper_636_<String, GenModel_636_>() {
    override fun map(input: String): GenModel_636_ {
        val parts = input.split(":")
        return GenModel_636_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_636_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_636_,
    private val saveUseCase: GenSaveUseCase_636_,
    private val deleteUseCase: GenDeleteUseCase_636_,
    private val searchUseCase: GenSearchUseCase_636_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_636_>(GenState_636_.Idle)
    val state: StateFlow<GenState_636_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_636_) {
        when (event) {
            is GenEvent_636_.Load -> loadAll()
            is GenEvent_636_.Update -> save(event.model)
            is GenEvent_636_.Delete -> delete(event.id)
            is GenEvent_636_.Refresh -> loadAll()
            is GenEvent_636_.Search -> search(event.query)
            is GenEvent_636_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_636_.Loading; _state.value = GenState_636_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_636_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_636_.Success(searchUseCase(query)) } }
}
