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

data class GenModel_3267_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3267_ {
    data class Load(val id: Long) : GenEvent_3267_()
    data class Update(val model: GenModel_3267_) : GenEvent_3267_()
    data class Delete(val id: Long) : GenEvent_3267_()
    data object Refresh : GenEvent_3267_()
    data class Search(val query: String) : GenEvent_3267_()
    data class Filter(val predicate: String) : GenEvent_3267_()
}

sealed class GenState_3267_ {
    data object Idle : GenState_3267_()
    data object Loading : GenState_3267_()
    data class Success(val items: List<GenModel_3267_>) : GenState_3267_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3267_()
    data class Partial(val items: List<GenModel_3267_>, val hasMore: Boolean) : GenState_3267_()
}

interface GenRepository_3267_ {
    suspend fun getAll(): List<GenModel_3267_>
    suspend fun getById(id: Long): GenModel_3267_?
    suspend fun save(model: GenModel_3267_): GenModel_3267_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3267_>
}

@Singleton
class GenRepositoryImpl_3267_ @Inject constructor() : GenRepository_3267_ {
    private val store = mutableMapOf<Long, GenModel_3267_>()
    override suspend fun getAll(): List<GenModel_3267_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3267_? = store[id]
    override suspend fun save(model: GenModel_3267_): GenModel_3267_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3267_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3267_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3267_ @Inject constructor(
    private val repository: GenRepositoryImpl_3267_
) : GenUseCase_3267_<Unit, List<GenModel_3267_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3267_> = repository.getAll()
}

class GenSaveUseCase_3267_ @Inject constructor(
    private val repository: GenRepositoryImpl_3267_
) : GenUseCase_3267_<GenModel_3267_, GenModel_3267_> {
    override suspend fun invoke(params: GenModel_3267_): GenModel_3267_ = repository.save(params)
}

class GenDeleteUseCase_3267_ @Inject constructor(
    private val repository: GenRepositoryImpl_3267_
) : GenUseCase_3267_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3267_ @Inject constructor(
    private val repository: GenRepositoryImpl_3267_
) : GenUseCase_3267_<String, List<GenModel_3267_>> {
    override suspend fun invoke(params: String): List<GenModel_3267_> = repository.search(params)
}

abstract class GenMapper_3267_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3267_ : GenMapper_3267_<GenModel_3267_, String>() {
    override fun map(input: GenModel_3267_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3267_ : GenMapper_3267_<String, GenModel_3267_>() {
    override fun map(input: String): GenModel_3267_ {
        val parts = input.split(":")
        return GenModel_3267_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3267_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3267_,
    private val saveUseCase: GenSaveUseCase_3267_,
    private val deleteUseCase: GenDeleteUseCase_3267_,
    private val searchUseCase: GenSearchUseCase_3267_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3267_>(GenState_3267_.Idle)
    val state: StateFlow<GenState_3267_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3267_) {
        when (event) {
            is GenEvent_3267_.Load -> loadAll()
            is GenEvent_3267_.Update -> save(event.model)
            is GenEvent_3267_.Delete -> delete(event.id)
            is GenEvent_3267_.Refresh -> loadAll()
            is GenEvent_3267_.Search -> search(event.query)
            is GenEvent_3267_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3267_.Loading; _state.value = GenState_3267_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3267_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3267_.Success(searchUseCase(query)) } }
}
