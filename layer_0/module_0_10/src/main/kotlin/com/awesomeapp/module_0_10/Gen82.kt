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

data class GenModel_82_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_82_ {
    data class Load(val id: Long) : GenEvent_82_()
    data class Update(val model: GenModel_82_) : GenEvent_82_()
    data class Delete(val id: Long) : GenEvent_82_()
    data object Refresh : GenEvent_82_()
    data class Search(val query: String) : GenEvent_82_()
    data class Filter(val predicate: String) : GenEvent_82_()
}

sealed class GenState_82_ {
    data object Idle : GenState_82_()
    data object Loading : GenState_82_()
    data class Success(val items: List<GenModel_82_>) : GenState_82_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_82_()
    data class Partial(val items: List<GenModel_82_>, val hasMore: Boolean) : GenState_82_()
}

interface GenRepository_82_ {
    suspend fun getAll(): List<GenModel_82_>
    suspend fun getById(id: Long): GenModel_82_?
    suspend fun save(model: GenModel_82_): GenModel_82_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_82_>
}

@Singleton
class GenRepositoryImpl_82_ @Inject constructor() : GenRepository_82_ {
    private val store = mutableMapOf<Long, GenModel_82_>()
    override suspend fun getAll(): List<GenModel_82_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_82_? = store[id]
    override suspend fun save(model: GenModel_82_): GenModel_82_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_82_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_82_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_82_ @Inject constructor(
    private val repository: GenRepositoryImpl_82_
) : GenUseCase_82_<Unit, List<GenModel_82_>> {
    override suspend fun invoke(params: Unit): List<GenModel_82_> = repository.getAll()
}

class GenSaveUseCase_82_ @Inject constructor(
    private val repository: GenRepositoryImpl_82_
) : GenUseCase_82_<GenModel_82_, GenModel_82_> {
    override suspend fun invoke(params: GenModel_82_): GenModel_82_ = repository.save(params)
}

class GenDeleteUseCase_82_ @Inject constructor(
    private val repository: GenRepositoryImpl_82_
) : GenUseCase_82_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_82_ @Inject constructor(
    private val repository: GenRepositoryImpl_82_
) : GenUseCase_82_<String, List<GenModel_82_>> {
    override suspend fun invoke(params: String): List<GenModel_82_> = repository.search(params)
}

abstract class GenMapper_82_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_82_ : GenMapper_82_<GenModel_82_, String>() {
    override fun map(input: GenModel_82_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_82_ : GenMapper_82_<String, GenModel_82_>() {
    override fun map(input: String): GenModel_82_ {
        val parts = input.split(":")
        return GenModel_82_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_82_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_82_,
    private val saveUseCase: GenSaveUseCase_82_,
    private val deleteUseCase: GenDeleteUseCase_82_,
    private val searchUseCase: GenSearchUseCase_82_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_82_>(GenState_82_.Idle)
    val state: StateFlow<GenState_82_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_82_) {
        when (event) {
            is GenEvent_82_.Load -> loadAll()
            is GenEvent_82_.Update -> save(event.model)
            is GenEvent_82_.Delete -> delete(event.id)
            is GenEvent_82_.Refresh -> loadAll()
            is GenEvent_82_.Search -> search(event.query)
            is GenEvent_82_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_82_.Loading; _state.value = GenState_82_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_82_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_82_.Success(searchUseCase(query)) } }
}
