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

data class GenModel_1807_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1807_ {
    data class Load(val id: Long) : GenEvent_1807_()
    data class Update(val model: GenModel_1807_) : GenEvent_1807_()
    data class Delete(val id: Long) : GenEvent_1807_()
    data object Refresh : GenEvent_1807_()
    data class Search(val query: String) : GenEvent_1807_()
    data class Filter(val predicate: String) : GenEvent_1807_()
}

sealed class GenState_1807_ {
    data object Idle : GenState_1807_()
    data object Loading : GenState_1807_()
    data class Success(val items: List<GenModel_1807_>) : GenState_1807_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1807_()
    data class Partial(val items: List<GenModel_1807_>, val hasMore: Boolean) : GenState_1807_()
}

interface GenRepository_1807_ {
    suspend fun getAll(): List<GenModel_1807_>
    suspend fun getById(id: Long): GenModel_1807_?
    suspend fun save(model: GenModel_1807_): GenModel_1807_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1807_>
}

@Singleton
class GenRepositoryImpl_1807_ @Inject constructor() : GenRepository_1807_ {
    private val store = mutableMapOf<Long, GenModel_1807_>()
    override suspend fun getAll(): List<GenModel_1807_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1807_? = store[id]
    override suspend fun save(model: GenModel_1807_): GenModel_1807_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1807_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1807_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1807_ @Inject constructor(
    private val repository: GenRepositoryImpl_1807_
) : GenUseCase_1807_<Unit, List<GenModel_1807_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1807_> = repository.getAll()
}

class GenSaveUseCase_1807_ @Inject constructor(
    private val repository: GenRepositoryImpl_1807_
) : GenUseCase_1807_<GenModel_1807_, GenModel_1807_> {
    override suspend fun invoke(params: GenModel_1807_): GenModel_1807_ = repository.save(params)
}

class GenDeleteUseCase_1807_ @Inject constructor(
    private val repository: GenRepositoryImpl_1807_
) : GenUseCase_1807_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1807_ @Inject constructor(
    private val repository: GenRepositoryImpl_1807_
) : GenUseCase_1807_<String, List<GenModel_1807_>> {
    override suspend fun invoke(params: String): List<GenModel_1807_> = repository.search(params)
}

abstract class GenMapper_1807_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1807_ : GenMapper_1807_<GenModel_1807_, String>() {
    override fun map(input: GenModel_1807_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1807_ : GenMapper_1807_<String, GenModel_1807_>() {
    override fun map(input: String): GenModel_1807_ {
        val parts = input.split(":")
        return GenModel_1807_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1807_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1807_,
    private val saveUseCase: GenSaveUseCase_1807_,
    private val deleteUseCase: GenDeleteUseCase_1807_,
    private val searchUseCase: GenSearchUseCase_1807_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1807_>(GenState_1807_.Idle)
    val state: StateFlow<GenState_1807_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1807_) {
        when (event) {
            is GenEvent_1807_.Load -> loadAll()
            is GenEvent_1807_.Update -> save(event.model)
            is GenEvent_1807_.Delete -> delete(event.id)
            is GenEvent_1807_.Refresh -> loadAll()
            is GenEvent_1807_.Search -> search(event.query)
            is GenEvent_1807_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1807_.Loading; _state.value = GenState_1807_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1807_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1807_.Success(searchUseCase(query)) } }
}
