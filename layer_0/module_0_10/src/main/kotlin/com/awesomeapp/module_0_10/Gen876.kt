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

data class GenModel_876_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_876_ {
    data class Load(val id: Long) : GenEvent_876_()
    data class Update(val model: GenModel_876_) : GenEvent_876_()
    data class Delete(val id: Long) : GenEvent_876_()
    data object Refresh : GenEvent_876_()
    data class Search(val query: String) : GenEvent_876_()
    data class Filter(val predicate: String) : GenEvent_876_()
}

sealed class GenState_876_ {
    data object Idle : GenState_876_()
    data object Loading : GenState_876_()
    data class Success(val items: List<GenModel_876_>) : GenState_876_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_876_()
    data class Partial(val items: List<GenModel_876_>, val hasMore: Boolean) : GenState_876_()
}

interface GenRepository_876_ {
    suspend fun getAll(): List<GenModel_876_>
    suspend fun getById(id: Long): GenModel_876_?
    suspend fun save(model: GenModel_876_): GenModel_876_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_876_>
}

@Singleton
class GenRepositoryImpl_876_ @Inject constructor() : GenRepository_876_ {
    private val store = mutableMapOf<Long, GenModel_876_>()
    override suspend fun getAll(): List<GenModel_876_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_876_? = store[id]
    override suspend fun save(model: GenModel_876_): GenModel_876_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_876_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_876_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_876_ @Inject constructor(
    private val repository: GenRepositoryImpl_876_
) : GenUseCase_876_<Unit, List<GenModel_876_>> {
    override suspend fun invoke(params: Unit): List<GenModel_876_> = repository.getAll()
}

class GenSaveUseCase_876_ @Inject constructor(
    private val repository: GenRepositoryImpl_876_
) : GenUseCase_876_<GenModel_876_, GenModel_876_> {
    override suspend fun invoke(params: GenModel_876_): GenModel_876_ = repository.save(params)
}

class GenDeleteUseCase_876_ @Inject constructor(
    private val repository: GenRepositoryImpl_876_
) : GenUseCase_876_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_876_ @Inject constructor(
    private val repository: GenRepositoryImpl_876_
) : GenUseCase_876_<String, List<GenModel_876_>> {
    override suspend fun invoke(params: String): List<GenModel_876_> = repository.search(params)
}

abstract class GenMapper_876_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_876_ : GenMapper_876_<GenModel_876_, String>() {
    override fun map(input: GenModel_876_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_876_ : GenMapper_876_<String, GenModel_876_>() {
    override fun map(input: String): GenModel_876_ {
        val parts = input.split(":")
        return GenModel_876_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_876_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_876_,
    private val saveUseCase: GenSaveUseCase_876_,
    private val deleteUseCase: GenDeleteUseCase_876_,
    private val searchUseCase: GenSearchUseCase_876_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_876_>(GenState_876_.Idle)
    val state: StateFlow<GenState_876_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_876_) {
        when (event) {
            is GenEvent_876_.Load -> loadAll()
            is GenEvent_876_.Update -> save(event.model)
            is GenEvent_876_.Delete -> delete(event.id)
            is GenEvent_876_.Refresh -> loadAll()
            is GenEvent_876_.Search -> search(event.query)
            is GenEvent_876_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_876_.Loading; _state.value = GenState_876_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_876_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_876_.Success(searchUseCase(query)) } }
}
