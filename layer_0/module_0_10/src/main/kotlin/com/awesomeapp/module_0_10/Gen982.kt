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

data class GenModel_982_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_982_ {
    data class Load(val id: Long) : GenEvent_982_()
    data class Update(val model: GenModel_982_) : GenEvent_982_()
    data class Delete(val id: Long) : GenEvent_982_()
    data object Refresh : GenEvent_982_()
    data class Search(val query: String) : GenEvent_982_()
    data class Filter(val predicate: String) : GenEvent_982_()
}

sealed class GenState_982_ {
    data object Idle : GenState_982_()
    data object Loading : GenState_982_()
    data class Success(val items: List<GenModel_982_>) : GenState_982_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_982_()
    data class Partial(val items: List<GenModel_982_>, val hasMore: Boolean) : GenState_982_()
}

interface GenRepository_982_ {
    suspend fun getAll(): List<GenModel_982_>
    suspend fun getById(id: Long): GenModel_982_?
    suspend fun save(model: GenModel_982_): GenModel_982_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_982_>
}

@Singleton
class GenRepositoryImpl_982_ @Inject constructor() : GenRepository_982_ {
    private val store = mutableMapOf<Long, GenModel_982_>()
    override suspend fun getAll(): List<GenModel_982_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_982_? = store[id]
    override suspend fun save(model: GenModel_982_): GenModel_982_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_982_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_982_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_982_ @Inject constructor(
    private val repository: GenRepositoryImpl_982_
) : GenUseCase_982_<Unit, List<GenModel_982_>> {
    override suspend fun invoke(params: Unit): List<GenModel_982_> = repository.getAll()
}

class GenSaveUseCase_982_ @Inject constructor(
    private val repository: GenRepositoryImpl_982_
) : GenUseCase_982_<GenModel_982_, GenModel_982_> {
    override suspend fun invoke(params: GenModel_982_): GenModel_982_ = repository.save(params)
}

class GenDeleteUseCase_982_ @Inject constructor(
    private val repository: GenRepositoryImpl_982_
) : GenUseCase_982_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_982_ @Inject constructor(
    private val repository: GenRepositoryImpl_982_
) : GenUseCase_982_<String, List<GenModel_982_>> {
    override suspend fun invoke(params: String): List<GenModel_982_> = repository.search(params)
}

abstract class GenMapper_982_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_982_ : GenMapper_982_<GenModel_982_, String>() {
    override fun map(input: GenModel_982_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_982_ : GenMapper_982_<String, GenModel_982_>() {
    override fun map(input: String): GenModel_982_ {
        val parts = input.split(":")
        return GenModel_982_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_982_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_982_,
    private val saveUseCase: GenSaveUseCase_982_,
    private val deleteUseCase: GenDeleteUseCase_982_,
    private val searchUseCase: GenSearchUseCase_982_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_982_>(GenState_982_.Idle)
    val state: StateFlow<GenState_982_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_982_) {
        when (event) {
            is GenEvent_982_.Load -> loadAll()
            is GenEvent_982_.Update -> save(event.model)
            is GenEvent_982_.Delete -> delete(event.id)
            is GenEvent_982_.Refresh -> loadAll()
            is GenEvent_982_.Search -> search(event.query)
            is GenEvent_982_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_982_.Loading; _state.value = GenState_982_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_982_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_982_.Success(searchUseCase(query)) } }
}
