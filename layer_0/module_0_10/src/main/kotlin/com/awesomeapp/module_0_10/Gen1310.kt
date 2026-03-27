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

data class GenModel_1310_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1310_ {
    data class Load(val id: Long) : GenEvent_1310_()
    data class Update(val model: GenModel_1310_) : GenEvent_1310_()
    data class Delete(val id: Long) : GenEvent_1310_()
    data object Refresh : GenEvent_1310_()
    data class Search(val query: String) : GenEvent_1310_()
    data class Filter(val predicate: String) : GenEvent_1310_()
}

sealed class GenState_1310_ {
    data object Idle : GenState_1310_()
    data object Loading : GenState_1310_()
    data class Success(val items: List<GenModel_1310_>) : GenState_1310_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1310_()
    data class Partial(val items: List<GenModel_1310_>, val hasMore: Boolean) : GenState_1310_()
}

interface GenRepository_1310_ {
    suspend fun getAll(): List<GenModel_1310_>
    suspend fun getById(id: Long): GenModel_1310_?
    suspend fun save(model: GenModel_1310_): GenModel_1310_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1310_>
}

@Singleton
class GenRepositoryImpl_1310_ @Inject constructor() : GenRepository_1310_ {
    private val store = mutableMapOf<Long, GenModel_1310_>()
    override suspend fun getAll(): List<GenModel_1310_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1310_? = store[id]
    override suspend fun save(model: GenModel_1310_): GenModel_1310_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1310_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1310_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1310_ @Inject constructor(
    private val repository: GenRepositoryImpl_1310_
) : GenUseCase_1310_<Unit, List<GenModel_1310_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1310_> = repository.getAll()
}

class GenSaveUseCase_1310_ @Inject constructor(
    private val repository: GenRepositoryImpl_1310_
) : GenUseCase_1310_<GenModel_1310_, GenModel_1310_> {
    override suspend fun invoke(params: GenModel_1310_): GenModel_1310_ = repository.save(params)
}

class GenDeleteUseCase_1310_ @Inject constructor(
    private val repository: GenRepositoryImpl_1310_
) : GenUseCase_1310_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1310_ @Inject constructor(
    private val repository: GenRepositoryImpl_1310_
) : GenUseCase_1310_<String, List<GenModel_1310_>> {
    override suspend fun invoke(params: String): List<GenModel_1310_> = repository.search(params)
}

abstract class GenMapper_1310_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1310_ : GenMapper_1310_<GenModel_1310_, String>() {
    override fun map(input: GenModel_1310_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1310_ : GenMapper_1310_<String, GenModel_1310_>() {
    override fun map(input: String): GenModel_1310_ {
        val parts = input.split(":")
        return GenModel_1310_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1310_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1310_,
    private val saveUseCase: GenSaveUseCase_1310_,
    private val deleteUseCase: GenDeleteUseCase_1310_,
    private val searchUseCase: GenSearchUseCase_1310_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1310_>(GenState_1310_.Idle)
    val state: StateFlow<GenState_1310_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1310_) {
        when (event) {
            is GenEvent_1310_.Load -> loadAll()
            is GenEvent_1310_.Update -> save(event.model)
            is GenEvent_1310_.Delete -> delete(event.id)
            is GenEvent_1310_.Refresh -> loadAll()
            is GenEvent_1310_.Search -> search(event.query)
            is GenEvent_1310_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1310_.Loading; _state.value = GenState_1310_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1310_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1310_.Success(searchUseCase(query)) } }
}
