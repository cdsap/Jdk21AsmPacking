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

data class GenModel_649_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_649_ {
    data class Load(val id: Long) : GenEvent_649_()
    data class Update(val model: GenModel_649_) : GenEvent_649_()
    data class Delete(val id: Long) : GenEvent_649_()
    data object Refresh : GenEvent_649_()
    data class Search(val query: String) : GenEvent_649_()
    data class Filter(val predicate: String) : GenEvent_649_()
}

sealed class GenState_649_ {
    data object Idle : GenState_649_()
    data object Loading : GenState_649_()
    data class Success(val items: List<GenModel_649_>) : GenState_649_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_649_()
    data class Partial(val items: List<GenModel_649_>, val hasMore: Boolean) : GenState_649_()
}

interface GenRepository_649_ {
    suspend fun getAll(): List<GenModel_649_>
    suspend fun getById(id: Long): GenModel_649_?
    suspend fun save(model: GenModel_649_): GenModel_649_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_649_>
}

@Singleton
class GenRepositoryImpl_649_ @Inject constructor() : GenRepository_649_ {
    private val store = mutableMapOf<Long, GenModel_649_>()
    override suspend fun getAll(): List<GenModel_649_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_649_? = store[id]
    override suspend fun save(model: GenModel_649_): GenModel_649_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_649_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_649_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_649_ @Inject constructor(
    private val repository: GenRepositoryImpl_649_
) : GenUseCase_649_<Unit, List<GenModel_649_>> {
    override suspend fun invoke(params: Unit): List<GenModel_649_> = repository.getAll()
}

class GenSaveUseCase_649_ @Inject constructor(
    private val repository: GenRepositoryImpl_649_
) : GenUseCase_649_<GenModel_649_, GenModel_649_> {
    override suspend fun invoke(params: GenModel_649_): GenModel_649_ = repository.save(params)
}

class GenDeleteUseCase_649_ @Inject constructor(
    private val repository: GenRepositoryImpl_649_
) : GenUseCase_649_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_649_ @Inject constructor(
    private val repository: GenRepositoryImpl_649_
) : GenUseCase_649_<String, List<GenModel_649_>> {
    override suspend fun invoke(params: String): List<GenModel_649_> = repository.search(params)
}

abstract class GenMapper_649_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_649_ : GenMapper_649_<GenModel_649_, String>() {
    override fun map(input: GenModel_649_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_649_ : GenMapper_649_<String, GenModel_649_>() {
    override fun map(input: String): GenModel_649_ {
        val parts = input.split(":")
        return GenModel_649_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_649_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_649_,
    private val saveUseCase: GenSaveUseCase_649_,
    private val deleteUseCase: GenDeleteUseCase_649_,
    private val searchUseCase: GenSearchUseCase_649_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_649_>(GenState_649_.Idle)
    val state: StateFlow<GenState_649_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_649_) {
        when (event) {
            is GenEvent_649_.Load -> loadAll()
            is GenEvent_649_.Update -> save(event.model)
            is GenEvent_649_.Delete -> delete(event.id)
            is GenEvent_649_.Refresh -> loadAll()
            is GenEvent_649_.Search -> search(event.query)
            is GenEvent_649_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_649_.Loading; _state.value = GenState_649_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_649_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_649_.Success(searchUseCase(query)) } }
}
