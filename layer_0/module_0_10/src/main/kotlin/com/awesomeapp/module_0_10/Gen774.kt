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

data class GenModel_774_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_774_ {
    data class Load(val id: Long) : GenEvent_774_()
    data class Update(val model: GenModel_774_) : GenEvent_774_()
    data class Delete(val id: Long) : GenEvent_774_()
    data object Refresh : GenEvent_774_()
    data class Search(val query: String) : GenEvent_774_()
    data class Filter(val predicate: String) : GenEvent_774_()
}

sealed class GenState_774_ {
    data object Idle : GenState_774_()
    data object Loading : GenState_774_()
    data class Success(val items: List<GenModel_774_>) : GenState_774_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_774_()
    data class Partial(val items: List<GenModel_774_>, val hasMore: Boolean) : GenState_774_()
}

interface GenRepository_774_ {
    suspend fun getAll(): List<GenModel_774_>
    suspend fun getById(id: Long): GenModel_774_?
    suspend fun save(model: GenModel_774_): GenModel_774_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_774_>
}

@Singleton
class GenRepositoryImpl_774_ @Inject constructor() : GenRepository_774_ {
    private val store = mutableMapOf<Long, GenModel_774_>()
    override suspend fun getAll(): List<GenModel_774_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_774_? = store[id]
    override suspend fun save(model: GenModel_774_): GenModel_774_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_774_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_774_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_774_ @Inject constructor(
    private val repository: GenRepositoryImpl_774_
) : GenUseCase_774_<Unit, List<GenModel_774_>> {
    override suspend fun invoke(params: Unit): List<GenModel_774_> = repository.getAll()
}

class GenSaveUseCase_774_ @Inject constructor(
    private val repository: GenRepositoryImpl_774_
) : GenUseCase_774_<GenModel_774_, GenModel_774_> {
    override suspend fun invoke(params: GenModel_774_): GenModel_774_ = repository.save(params)
}

class GenDeleteUseCase_774_ @Inject constructor(
    private val repository: GenRepositoryImpl_774_
) : GenUseCase_774_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_774_ @Inject constructor(
    private val repository: GenRepositoryImpl_774_
) : GenUseCase_774_<String, List<GenModel_774_>> {
    override suspend fun invoke(params: String): List<GenModel_774_> = repository.search(params)
}

abstract class GenMapper_774_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_774_ : GenMapper_774_<GenModel_774_, String>() {
    override fun map(input: GenModel_774_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_774_ : GenMapper_774_<String, GenModel_774_>() {
    override fun map(input: String): GenModel_774_ {
        val parts = input.split(":")
        return GenModel_774_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_774_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_774_,
    private val saveUseCase: GenSaveUseCase_774_,
    private val deleteUseCase: GenDeleteUseCase_774_,
    private val searchUseCase: GenSearchUseCase_774_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_774_>(GenState_774_.Idle)
    val state: StateFlow<GenState_774_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_774_) {
        when (event) {
            is GenEvent_774_.Load -> loadAll()
            is GenEvent_774_.Update -> save(event.model)
            is GenEvent_774_.Delete -> delete(event.id)
            is GenEvent_774_.Refresh -> loadAll()
            is GenEvent_774_.Search -> search(event.query)
            is GenEvent_774_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_774_.Loading; _state.value = GenState_774_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_774_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_774_.Success(searchUseCase(query)) } }
}
