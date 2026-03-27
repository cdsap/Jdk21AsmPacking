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

data class GenModel_1837_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1837_ {
    data class Load(val id: Long) : GenEvent_1837_()
    data class Update(val model: GenModel_1837_) : GenEvent_1837_()
    data class Delete(val id: Long) : GenEvent_1837_()
    data object Refresh : GenEvent_1837_()
    data class Search(val query: String) : GenEvent_1837_()
    data class Filter(val predicate: String) : GenEvent_1837_()
}

sealed class GenState_1837_ {
    data object Idle : GenState_1837_()
    data object Loading : GenState_1837_()
    data class Success(val items: List<GenModel_1837_>) : GenState_1837_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1837_()
    data class Partial(val items: List<GenModel_1837_>, val hasMore: Boolean) : GenState_1837_()
}

interface GenRepository_1837_ {
    suspend fun getAll(): List<GenModel_1837_>
    suspend fun getById(id: Long): GenModel_1837_?
    suspend fun save(model: GenModel_1837_): GenModel_1837_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1837_>
}

@Singleton
class GenRepositoryImpl_1837_ @Inject constructor() : GenRepository_1837_ {
    private val store = mutableMapOf<Long, GenModel_1837_>()
    override suspend fun getAll(): List<GenModel_1837_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1837_? = store[id]
    override suspend fun save(model: GenModel_1837_): GenModel_1837_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1837_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1837_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1837_ @Inject constructor(
    private val repository: GenRepositoryImpl_1837_
) : GenUseCase_1837_<Unit, List<GenModel_1837_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1837_> = repository.getAll()
}

class GenSaveUseCase_1837_ @Inject constructor(
    private val repository: GenRepositoryImpl_1837_
) : GenUseCase_1837_<GenModel_1837_, GenModel_1837_> {
    override suspend fun invoke(params: GenModel_1837_): GenModel_1837_ = repository.save(params)
}

class GenDeleteUseCase_1837_ @Inject constructor(
    private val repository: GenRepositoryImpl_1837_
) : GenUseCase_1837_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1837_ @Inject constructor(
    private val repository: GenRepositoryImpl_1837_
) : GenUseCase_1837_<String, List<GenModel_1837_>> {
    override suspend fun invoke(params: String): List<GenModel_1837_> = repository.search(params)
}

abstract class GenMapper_1837_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1837_ : GenMapper_1837_<GenModel_1837_, String>() {
    override fun map(input: GenModel_1837_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1837_ : GenMapper_1837_<String, GenModel_1837_>() {
    override fun map(input: String): GenModel_1837_ {
        val parts = input.split(":")
        return GenModel_1837_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1837_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1837_,
    private val saveUseCase: GenSaveUseCase_1837_,
    private val deleteUseCase: GenDeleteUseCase_1837_,
    private val searchUseCase: GenSearchUseCase_1837_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1837_>(GenState_1837_.Idle)
    val state: StateFlow<GenState_1837_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1837_) {
        when (event) {
            is GenEvent_1837_.Load -> loadAll()
            is GenEvent_1837_.Update -> save(event.model)
            is GenEvent_1837_.Delete -> delete(event.id)
            is GenEvent_1837_.Refresh -> loadAll()
            is GenEvent_1837_.Search -> search(event.query)
            is GenEvent_1837_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1837_.Loading; _state.value = GenState_1837_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1837_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1837_.Success(searchUseCase(query)) } }
}
