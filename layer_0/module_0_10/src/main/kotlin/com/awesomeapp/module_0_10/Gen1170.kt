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

data class GenModel_1170_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1170_ {
    data class Load(val id: Long) : GenEvent_1170_()
    data class Update(val model: GenModel_1170_) : GenEvent_1170_()
    data class Delete(val id: Long) : GenEvent_1170_()
    data object Refresh : GenEvent_1170_()
    data class Search(val query: String) : GenEvent_1170_()
    data class Filter(val predicate: String) : GenEvent_1170_()
}

sealed class GenState_1170_ {
    data object Idle : GenState_1170_()
    data object Loading : GenState_1170_()
    data class Success(val items: List<GenModel_1170_>) : GenState_1170_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1170_()
    data class Partial(val items: List<GenModel_1170_>, val hasMore: Boolean) : GenState_1170_()
}

interface GenRepository_1170_ {
    suspend fun getAll(): List<GenModel_1170_>
    suspend fun getById(id: Long): GenModel_1170_?
    suspend fun save(model: GenModel_1170_): GenModel_1170_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1170_>
}

@Singleton
class GenRepositoryImpl_1170_ @Inject constructor() : GenRepository_1170_ {
    private val store = mutableMapOf<Long, GenModel_1170_>()
    override suspend fun getAll(): List<GenModel_1170_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1170_? = store[id]
    override suspend fun save(model: GenModel_1170_): GenModel_1170_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1170_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1170_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1170_ @Inject constructor(
    private val repository: GenRepositoryImpl_1170_
) : GenUseCase_1170_<Unit, List<GenModel_1170_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1170_> = repository.getAll()
}

class GenSaveUseCase_1170_ @Inject constructor(
    private val repository: GenRepositoryImpl_1170_
) : GenUseCase_1170_<GenModel_1170_, GenModel_1170_> {
    override suspend fun invoke(params: GenModel_1170_): GenModel_1170_ = repository.save(params)
}

class GenDeleteUseCase_1170_ @Inject constructor(
    private val repository: GenRepositoryImpl_1170_
) : GenUseCase_1170_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1170_ @Inject constructor(
    private val repository: GenRepositoryImpl_1170_
) : GenUseCase_1170_<String, List<GenModel_1170_>> {
    override suspend fun invoke(params: String): List<GenModel_1170_> = repository.search(params)
}

abstract class GenMapper_1170_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1170_ : GenMapper_1170_<GenModel_1170_, String>() {
    override fun map(input: GenModel_1170_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1170_ : GenMapper_1170_<String, GenModel_1170_>() {
    override fun map(input: String): GenModel_1170_ {
        val parts = input.split(":")
        return GenModel_1170_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1170_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1170_,
    private val saveUseCase: GenSaveUseCase_1170_,
    private val deleteUseCase: GenDeleteUseCase_1170_,
    private val searchUseCase: GenSearchUseCase_1170_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1170_>(GenState_1170_.Idle)
    val state: StateFlow<GenState_1170_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1170_) {
        when (event) {
            is GenEvent_1170_.Load -> loadAll()
            is GenEvent_1170_.Update -> save(event.model)
            is GenEvent_1170_.Delete -> delete(event.id)
            is GenEvent_1170_.Refresh -> loadAll()
            is GenEvent_1170_.Search -> search(event.query)
            is GenEvent_1170_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1170_.Loading; _state.value = GenState_1170_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1170_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1170_.Success(searchUseCase(query)) } }
}
