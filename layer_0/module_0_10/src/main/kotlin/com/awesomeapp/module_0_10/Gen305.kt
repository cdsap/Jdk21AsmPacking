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

data class GenModel_305_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_305_ {
    data class Load(val id: Long) : GenEvent_305_()
    data class Update(val model: GenModel_305_) : GenEvent_305_()
    data class Delete(val id: Long) : GenEvent_305_()
    data object Refresh : GenEvent_305_()
    data class Search(val query: String) : GenEvent_305_()
    data class Filter(val predicate: String) : GenEvent_305_()
}

sealed class GenState_305_ {
    data object Idle : GenState_305_()
    data object Loading : GenState_305_()
    data class Success(val items: List<GenModel_305_>) : GenState_305_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_305_()
    data class Partial(val items: List<GenModel_305_>, val hasMore: Boolean) : GenState_305_()
}

interface GenRepository_305_ {
    suspend fun getAll(): List<GenModel_305_>
    suspend fun getById(id: Long): GenModel_305_?
    suspend fun save(model: GenModel_305_): GenModel_305_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_305_>
}

@Singleton
class GenRepositoryImpl_305_ @Inject constructor() : GenRepository_305_ {
    private val store = mutableMapOf<Long, GenModel_305_>()
    override suspend fun getAll(): List<GenModel_305_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_305_? = store[id]
    override suspend fun save(model: GenModel_305_): GenModel_305_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_305_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_305_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_305_ @Inject constructor(
    private val repository: GenRepositoryImpl_305_
) : GenUseCase_305_<Unit, List<GenModel_305_>> {
    override suspend fun invoke(params: Unit): List<GenModel_305_> = repository.getAll()
}

class GenSaveUseCase_305_ @Inject constructor(
    private val repository: GenRepositoryImpl_305_
) : GenUseCase_305_<GenModel_305_, GenModel_305_> {
    override suspend fun invoke(params: GenModel_305_): GenModel_305_ = repository.save(params)
}

class GenDeleteUseCase_305_ @Inject constructor(
    private val repository: GenRepositoryImpl_305_
) : GenUseCase_305_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_305_ @Inject constructor(
    private val repository: GenRepositoryImpl_305_
) : GenUseCase_305_<String, List<GenModel_305_>> {
    override suspend fun invoke(params: String): List<GenModel_305_> = repository.search(params)
}

abstract class GenMapper_305_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_305_ : GenMapper_305_<GenModel_305_, String>() {
    override fun map(input: GenModel_305_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_305_ : GenMapper_305_<String, GenModel_305_>() {
    override fun map(input: String): GenModel_305_ {
        val parts = input.split(":")
        return GenModel_305_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_305_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_305_,
    private val saveUseCase: GenSaveUseCase_305_,
    private val deleteUseCase: GenDeleteUseCase_305_,
    private val searchUseCase: GenSearchUseCase_305_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_305_>(GenState_305_.Idle)
    val state: StateFlow<GenState_305_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_305_) {
        when (event) {
            is GenEvent_305_.Load -> loadAll()
            is GenEvent_305_.Update -> save(event.model)
            is GenEvent_305_.Delete -> delete(event.id)
            is GenEvent_305_.Refresh -> loadAll()
            is GenEvent_305_.Search -> search(event.query)
            is GenEvent_305_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_305_.Loading; _state.value = GenState_305_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_305_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_305_.Success(searchUseCase(query)) } }
}
