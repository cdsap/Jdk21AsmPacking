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

data class GenModel_730_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_730_ {
    data class Load(val id: Long) : GenEvent_730_()
    data class Update(val model: GenModel_730_) : GenEvent_730_()
    data class Delete(val id: Long) : GenEvent_730_()
    data object Refresh : GenEvent_730_()
    data class Search(val query: String) : GenEvent_730_()
    data class Filter(val predicate: String) : GenEvent_730_()
}

sealed class GenState_730_ {
    data object Idle : GenState_730_()
    data object Loading : GenState_730_()
    data class Success(val items: List<GenModel_730_>) : GenState_730_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_730_()
    data class Partial(val items: List<GenModel_730_>, val hasMore: Boolean) : GenState_730_()
}

interface GenRepository_730_ {
    suspend fun getAll(): List<GenModel_730_>
    suspend fun getById(id: Long): GenModel_730_?
    suspend fun save(model: GenModel_730_): GenModel_730_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_730_>
}

@Singleton
class GenRepositoryImpl_730_ @Inject constructor() : GenRepository_730_ {
    private val store = mutableMapOf<Long, GenModel_730_>()
    override suspend fun getAll(): List<GenModel_730_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_730_? = store[id]
    override suspend fun save(model: GenModel_730_): GenModel_730_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_730_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_730_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_730_ @Inject constructor(
    private val repository: GenRepositoryImpl_730_
) : GenUseCase_730_<Unit, List<GenModel_730_>> {
    override suspend fun invoke(params: Unit): List<GenModel_730_> = repository.getAll()
}

class GenSaveUseCase_730_ @Inject constructor(
    private val repository: GenRepositoryImpl_730_
) : GenUseCase_730_<GenModel_730_, GenModel_730_> {
    override suspend fun invoke(params: GenModel_730_): GenModel_730_ = repository.save(params)
}

class GenDeleteUseCase_730_ @Inject constructor(
    private val repository: GenRepositoryImpl_730_
) : GenUseCase_730_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_730_ @Inject constructor(
    private val repository: GenRepositoryImpl_730_
) : GenUseCase_730_<String, List<GenModel_730_>> {
    override suspend fun invoke(params: String): List<GenModel_730_> = repository.search(params)
}

abstract class GenMapper_730_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_730_ : GenMapper_730_<GenModel_730_, String>() {
    override fun map(input: GenModel_730_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_730_ : GenMapper_730_<String, GenModel_730_>() {
    override fun map(input: String): GenModel_730_ {
        val parts = input.split(":")
        return GenModel_730_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_730_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_730_,
    private val saveUseCase: GenSaveUseCase_730_,
    private val deleteUseCase: GenDeleteUseCase_730_,
    private val searchUseCase: GenSearchUseCase_730_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_730_>(GenState_730_.Idle)
    val state: StateFlow<GenState_730_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_730_) {
        when (event) {
            is GenEvent_730_.Load -> loadAll()
            is GenEvent_730_.Update -> save(event.model)
            is GenEvent_730_.Delete -> delete(event.id)
            is GenEvent_730_.Refresh -> loadAll()
            is GenEvent_730_.Search -> search(event.query)
            is GenEvent_730_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_730_.Loading; _state.value = GenState_730_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_730_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_730_.Success(searchUseCase(query)) } }
}
