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

data class GenModel_310_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_310_ {
    data class Load(val id: Long) : GenEvent_310_()
    data class Update(val model: GenModel_310_) : GenEvent_310_()
    data class Delete(val id: Long) : GenEvent_310_()
    data object Refresh : GenEvent_310_()
    data class Search(val query: String) : GenEvent_310_()
    data class Filter(val predicate: String) : GenEvent_310_()
}

sealed class GenState_310_ {
    data object Idle : GenState_310_()
    data object Loading : GenState_310_()
    data class Success(val items: List<GenModel_310_>) : GenState_310_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_310_()
    data class Partial(val items: List<GenModel_310_>, val hasMore: Boolean) : GenState_310_()
}

interface GenRepository_310_ {
    suspend fun getAll(): List<GenModel_310_>
    suspend fun getById(id: Long): GenModel_310_?
    suspend fun save(model: GenModel_310_): GenModel_310_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_310_>
}

@Singleton
class GenRepositoryImpl_310_ @Inject constructor() : GenRepository_310_ {
    private val store = mutableMapOf<Long, GenModel_310_>()
    override suspend fun getAll(): List<GenModel_310_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_310_? = store[id]
    override suspend fun save(model: GenModel_310_): GenModel_310_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_310_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_310_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_310_ @Inject constructor(
    private val repository: GenRepositoryImpl_310_
) : GenUseCase_310_<Unit, List<GenModel_310_>> {
    override suspend fun invoke(params: Unit): List<GenModel_310_> = repository.getAll()
}

class GenSaveUseCase_310_ @Inject constructor(
    private val repository: GenRepositoryImpl_310_
) : GenUseCase_310_<GenModel_310_, GenModel_310_> {
    override suspend fun invoke(params: GenModel_310_): GenModel_310_ = repository.save(params)
}

class GenDeleteUseCase_310_ @Inject constructor(
    private val repository: GenRepositoryImpl_310_
) : GenUseCase_310_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_310_ @Inject constructor(
    private val repository: GenRepositoryImpl_310_
) : GenUseCase_310_<String, List<GenModel_310_>> {
    override suspend fun invoke(params: String): List<GenModel_310_> = repository.search(params)
}

abstract class GenMapper_310_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_310_ : GenMapper_310_<GenModel_310_, String>() {
    override fun map(input: GenModel_310_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_310_ : GenMapper_310_<String, GenModel_310_>() {
    override fun map(input: String): GenModel_310_ {
        val parts = input.split(":")
        return GenModel_310_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_310_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_310_,
    private val saveUseCase: GenSaveUseCase_310_,
    private val deleteUseCase: GenDeleteUseCase_310_,
    private val searchUseCase: GenSearchUseCase_310_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_310_>(GenState_310_.Idle)
    val state: StateFlow<GenState_310_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_310_) {
        when (event) {
            is GenEvent_310_.Load -> loadAll()
            is GenEvent_310_.Update -> save(event.model)
            is GenEvent_310_.Delete -> delete(event.id)
            is GenEvent_310_.Refresh -> loadAll()
            is GenEvent_310_.Search -> search(event.query)
            is GenEvent_310_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_310_.Loading; _state.value = GenState_310_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_310_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_310_.Success(searchUseCase(query)) } }
}
