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

data class GenModel_205_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_205_ {
    data class Load(val id: Long) : GenEvent_205_()
    data class Update(val model: GenModel_205_) : GenEvent_205_()
    data class Delete(val id: Long) : GenEvent_205_()
    data object Refresh : GenEvent_205_()
    data class Search(val query: String) : GenEvent_205_()
    data class Filter(val predicate: String) : GenEvent_205_()
}

sealed class GenState_205_ {
    data object Idle : GenState_205_()
    data object Loading : GenState_205_()
    data class Success(val items: List<GenModel_205_>) : GenState_205_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_205_()
    data class Partial(val items: List<GenModel_205_>, val hasMore: Boolean) : GenState_205_()
}

interface GenRepository_205_ {
    suspend fun getAll(): List<GenModel_205_>
    suspend fun getById(id: Long): GenModel_205_?
    suspend fun save(model: GenModel_205_): GenModel_205_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_205_>
}

@Singleton
class GenRepositoryImpl_205_ @Inject constructor() : GenRepository_205_ {
    private val store = mutableMapOf<Long, GenModel_205_>()
    override suspend fun getAll(): List<GenModel_205_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_205_? = store[id]
    override suspend fun save(model: GenModel_205_): GenModel_205_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_205_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_205_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_205_ @Inject constructor(
    private val repository: GenRepositoryImpl_205_
) : GenUseCase_205_<Unit, List<GenModel_205_>> {
    override suspend fun invoke(params: Unit): List<GenModel_205_> = repository.getAll()
}

class GenSaveUseCase_205_ @Inject constructor(
    private val repository: GenRepositoryImpl_205_
) : GenUseCase_205_<GenModel_205_, GenModel_205_> {
    override suspend fun invoke(params: GenModel_205_): GenModel_205_ = repository.save(params)
}

class GenDeleteUseCase_205_ @Inject constructor(
    private val repository: GenRepositoryImpl_205_
) : GenUseCase_205_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_205_ @Inject constructor(
    private val repository: GenRepositoryImpl_205_
) : GenUseCase_205_<String, List<GenModel_205_>> {
    override suspend fun invoke(params: String): List<GenModel_205_> = repository.search(params)
}

abstract class GenMapper_205_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_205_ : GenMapper_205_<GenModel_205_, String>() {
    override fun map(input: GenModel_205_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_205_ : GenMapper_205_<String, GenModel_205_>() {
    override fun map(input: String): GenModel_205_ {
        val parts = input.split(":")
        return GenModel_205_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_205_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_205_,
    private val saveUseCase: GenSaveUseCase_205_,
    private val deleteUseCase: GenDeleteUseCase_205_,
    private val searchUseCase: GenSearchUseCase_205_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_205_>(GenState_205_.Idle)
    val state: StateFlow<GenState_205_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_205_) {
        when (event) {
            is GenEvent_205_.Load -> loadAll()
            is GenEvent_205_.Update -> save(event.model)
            is GenEvent_205_.Delete -> delete(event.id)
            is GenEvent_205_.Refresh -> loadAll()
            is GenEvent_205_.Search -> search(event.query)
            is GenEvent_205_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_205_.Loading; _state.value = GenState_205_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_205_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_205_.Success(searchUseCase(query)) } }
}
