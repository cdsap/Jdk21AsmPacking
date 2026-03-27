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

data class GenModel_266_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_266_ {
    data class Load(val id: Long) : GenEvent_266_()
    data class Update(val model: GenModel_266_) : GenEvent_266_()
    data class Delete(val id: Long) : GenEvent_266_()
    data object Refresh : GenEvent_266_()
    data class Search(val query: String) : GenEvent_266_()
    data class Filter(val predicate: String) : GenEvent_266_()
}

sealed class GenState_266_ {
    data object Idle : GenState_266_()
    data object Loading : GenState_266_()
    data class Success(val items: List<GenModel_266_>) : GenState_266_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_266_()
    data class Partial(val items: List<GenModel_266_>, val hasMore: Boolean) : GenState_266_()
}

interface GenRepository_266_ {
    suspend fun getAll(): List<GenModel_266_>
    suspend fun getById(id: Long): GenModel_266_?
    suspend fun save(model: GenModel_266_): GenModel_266_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_266_>
}

@Singleton
class GenRepositoryImpl_266_ @Inject constructor() : GenRepository_266_ {
    private val store = mutableMapOf<Long, GenModel_266_>()
    override suspend fun getAll(): List<GenModel_266_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_266_? = store[id]
    override suspend fun save(model: GenModel_266_): GenModel_266_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_266_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_266_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_266_ @Inject constructor(
    private val repository: GenRepositoryImpl_266_
) : GenUseCase_266_<Unit, List<GenModel_266_>> {
    override suspend fun invoke(params: Unit): List<GenModel_266_> = repository.getAll()
}

class GenSaveUseCase_266_ @Inject constructor(
    private val repository: GenRepositoryImpl_266_
) : GenUseCase_266_<GenModel_266_, GenModel_266_> {
    override suspend fun invoke(params: GenModel_266_): GenModel_266_ = repository.save(params)
}

class GenDeleteUseCase_266_ @Inject constructor(
    private val repository: GenRepositoryImpl_266_
) : GenUseCase_266_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_266_ @Inject constructor(
    private val repository: GenRepositoryImpl_266_
) : GenUseCase_266_<String, List<GenModel_266_>> {
    override suspend fun invoke(params: String): List<GenModel_266_> = repository.search(params)
}

abstract class GenMapper_266_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_266_ : GenMapper_266_<GenModel_266_, String>() {
    override fun map(input: GenModel_266_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_266_ : GenMapper_266_<String, GenModel_266_>() {
    override fun map(input: String): GenModel_266_ {
        val parts = input.split(":")
        return GenModel_266_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_266_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_266_,
    private val saveUseCase: GenSaveUseCase_266_,
    private val deleteUseCase: GenDeleteUseCase_266_,
    private val searchUseCase: GenSearchUseCase_266_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_266_>(GenState_266_.Idle)
    val state: StateFlow<GenState_266_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_266_) {
        when (event) {
            is GenEvent_266_.Load -> loadAll()
            is GenEvent_266_.Update -> save(event.model)
            is GenEvent_266_.Delete -> delete(event.id)
            is GenEvent_266_.Refresh -> loadAll()
            is GenEvent_266_.Search -> search(event.query)
            is GenEvent_266_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_266_.Loading; _state.value = GenState_266_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_266_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_266_.Success(searchUseCase(query)) } }
}
