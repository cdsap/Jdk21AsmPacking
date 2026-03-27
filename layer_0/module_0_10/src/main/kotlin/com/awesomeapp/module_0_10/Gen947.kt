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

data class GenModel_947_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_947_ {
    data class Load(val id: Long) : GenEvent_947_()
    data class Update(val model: GenModel_947_) : GenEvent_947_()
    data class Delete(val id: Long) : GenEvent_947_()
    data object Refresh : GenEvent_947_()
    data class Search(val query: String) : GenEvent_947_()
    data class Filter(val predicate: String) : GenEvent_947_()
}

sealed class GenState_947_ {
    data object Idle : GenState_947_()
    data object Loading : GenState_947_()
    data class Success(val items: List<GenModel_947_>) : GenState_947_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_947_()
    data class Partial(val items: List<GenModel_947_>, val hasMore: Boolean) : GenState_947_()
}

interface GenRepository_947_ {
    suspend fun getAll(): List<GenModel_947_>
    suspend fun getById(id: Long): GenModel_947_?
    suspend fun save(model: GenModel_947_): GenModel_947_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_947_>
}

@Singleton
class GenRepositoryImpl_947_ @Inject constructor() : GenRepository_947_ {
    private val store = mutableMapOf<Long, GenModel_947_>()
    override suspend fun getAll(): List<GenModel_947_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_947_? = store[id]
    override suspend fun save(model: GenModel_947_): GenModel_947_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_947_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_947_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_947_ @Inject constructor(
    private val repository: GenRepositoryImpl_947_
) : GenUseCase_947_<Unit, List<GenModel_947_>> {
    override suspend fun invoke(params: Unit): List<GenModel_947_> = repository.getAll()
}

class GenSaveUseCase_947_ @Inject constructor(
    private val repository: GenRepositoryImpl_947_
) : GenUseCase_947_<GenModel_947_, GenModel_947_> {
    override suspend fun invoke(params: GenModel_947_): GenModel_947_ = repository.save(params)
}

class GenDeleteUseCase_947_ @Inject constructor(
    private val repository: GenRepositoryImpl_947_
) : GenUseCase_947_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_947_ @Inject constructor(
    private val repository: GenRepositoryImpl_947_
) : GenUseCase_947_<String, List<GenModel_947_>> {
    override suspend fun invoke(params: String): List<GenModel_947_> = repository.search(params)
}

abstract class GenMapper_947_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_947_ : GenMapper_947_<GenModel_947_, String>() {
    override fun map(input: GenModel_947_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_947_ : GenMapper_947_<String, GenModel_947_>() {
    override fun map(input: String): GenModel_947_ {
        val parts = input.split(":")
        return GenModel_947_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_947_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_947_,
    private val saveUseCase: GenSaveUseCase_947_,
    private val deleteUseCase: GenDeleteUseCase_947_,
    private val searchUseCase: GenSearchUseCase_947_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_947_>(GenState_947_.Idle)
    val state: StateFlow<GenState_947_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_947_) {
        when (event) {
            is GenEvent_947_.Load -> loadAll()
            is GenEvent_947_.Update -> save(event.model)
            is GenEvent_947_.Delete -> delete(event.id)
            is GenEvent_947_.Refresh -> loadAll()
            is GenEvent_947_.Search -> search(event.query)
            is GenEvent_947_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_947_.Loading; _state.value = GenState_947_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_947_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_947_.Success(searchUseCase(query)) } }
}
