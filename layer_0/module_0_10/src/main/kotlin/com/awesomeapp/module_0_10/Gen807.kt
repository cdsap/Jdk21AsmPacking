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

data class GenModel_807_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_807_ {
    data class Load(val id: Long) : GenEvent_807_()
    data class Update(val model: GenModel_807_) : GenEvent_807_()
    data class Delete(val id: Long) : GenEvent_807_()
    data object Refresh : GenEvent_807_()
    data class Search(val query: String) : GenEvent_807_()
    data class Filter(val predicate: String) : GenEvent_807_()
}

sealed class GenState_807_ {
    data object Idle : GenState_807_()
    data object Loading : GenState_807_()
    data class Success(val items: List<GenModel_807_>) : GenState_807_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_807_()
    data class Partial(val items: List<GenModel_807_>, val hasMore: Boolean) : GenState_807_()
}

interface GenRepository_807_ {
    suspend fun getAll(): List<GenModel_807_>
    suspend fun getById(id: Long): GenModel_807_?
    suspend fun save(model: GenModel_807_): GenModel_807_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_807_>
}

@Singleton
class GenRepositoryImpl_807_ @Inject constructor() : GenRepository_807_ {
    private val store = mutableMapOf<Long, GenModel_807_>()
    override suspend fun getAll(): List<GenModel_807_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_807_? = store[id]
    override suspend fun save(model: GenModel_807_): GenModel_807_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_807_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_807_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_807_ @Inject constructor(
    private val repository: GenRepositoryImpl_807_
) : GenUseCase_807_<Unit, List<GenModel_807_>> {
    override suspend fun invoke(params: Unit): List<GenModel_807_> = repository.getAll()
}

class GenSaveUseCase_807_ @Inject constructor(
    private val repository: GenRepositoryImpl_807_
) : GenUseCase_807_<GenModel_807_, GenModel_807_> {
    override suspend fun invoke(params: GenModel_807_): GenModel_807_ = repository.save(params)
}

class GenDeleteUseCase_807_ @Inject constructor(
    private val repository: GenRepositoryImpl_807_
) : GenUseCase_807_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_807_ @Inject constructor(
    private val repository: GenRepositoryImpl_807_
) : GenUseCase_807_<String, List<GenModel_807_>> {
    override suspend fun invoke(params: String): List<GenModel_807_> = repository.search(params)
}

abstract class GenMapper_807_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_807_ : GenMapper_807_<GenModel_807_, String>() {
    override fun map(input: GenModel_807_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_807_ : GenMapper_807_<String, GenModel_807_>() {
    override fun map(input: String): GenModel_807_ {
        val parts = input.split(":")
        return GenModel_807_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_807_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_807_,
    private val saveUseCase: GenSaveUseCase_807_,
    private val deleteUseCase: GenDeleteUseCase_807_,
    private val searchUseCase: GenSearchUseCase_807_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_807_>(GenState_807_.Idle)
    val state: StateFlow<GenState_807_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_807_) {
        when (event) {
            is GenEvent_807_.Load -> loadAll()
            is GenEvent_807_.Update -> save(event.model)
            is GenEvent_807_.Delete -> delete(event.id)
            is GenEvent_807_.Refresh -> loadAll()
            is GenEvent_807_.Search -> search(event.query)
            is GenEvent_807_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_807_.Loading; _state.value = GenState_807_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_807_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_807_.Success(searchUseCase(query)) } }
}
