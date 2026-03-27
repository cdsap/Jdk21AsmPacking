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

data class GenModel_576_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_576_ {
    data class Load(val id: Long) : GenEvent_576_()
    data class Update(val model: GenModel_576_) : GenEvent_576_()
    data class Delete(val id: Long) : GenEvent_576_()
    data object Refresh : GenEvent_576_()
    data class Search(val query: String) : GenEvent_576_()
    data class Filter(val predicate: String) : GenEvent_576_()
}

sealed class GenState_576_ {
    data object Idle : GenState_576_()
    data object Loading : GenState_576_()
    data class Success(val items: List<GenModel_576_>) : GenState_576_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_576_()
    data class Partial(val items: List<GenModel_576_>, val hasMore: Boolean) : GenState_576_()
}

interface GenRepository_576_ {
    suspend fun getAll(): List<GenModel_576_>
    suspend fun getById(id: Long): GenModel_576_?
    suspend fun save(model: GenModel_576_): GenModel_576_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_576_>
}

@Singleton
class GenRepositoryImpl_576_ @Inject constructor() : GenRepository_576_ {
    private val store = mutableMapOf<Long, GenModel_576_>()
    override suspend fun getAll(): List<GenModel_576_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_576_? = store[id]
    override suspend fun save(model: GenModel_576_): GenModel_576_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_576_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_576_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_576_ @Inject constructor(
    private val repository: GenRepositoryImpl_576_
) : GenUseCase_576_<Unit, List<GenModel_576_>> {
    override suspend fun invoke(params: Unit): List<GenModel_576_> = repository.getAll()
}

class GenSaveUseCase_576_ @Inject constructor(
    private val repository: GenRepositoryImpl_576_
) : GenUseCase_576_<GenModel_576_, GenModel_576_> {
    override suspend fun invoke(params: GenModel_576_): GenModel_576_ = repository.save(params)
}

class GenDeleteUseCase_576_ @Inject constructor(
    private val repository: GenRepositoryImpl_576_
) : GenUseCase_576_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_576_ @Inject constructor(
    private val repository: GenRepositoryImpl_576_
) : GenUseCase_576_<String, List<GenModel_576_>> {
    override suspend fun invoke(params: String): List<GenModel_576_> = repository.search(params)
}

abstract class GenMapper_576_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_576_ : GenMapper_576_<GenModel_576_, String>() {
    override fun map(input: GenModel_576_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_576_ : GenMapper_576_<String, GenModel_576_>() {
    override fun map(input: String): GenModel_576_ {
        val parts = input.split(":")
        return GenModel_576_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_576_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_576_,
    private val saveUseCase: GenSaveUseCase_576_,
    private val deleteUseCase: GenDeleteUseCase_576_,
    private val searchUseCase: GenSearchUseCase_576_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_576_>(GenState_576_.Idle)
    val state: StateFlow<GenState_576_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_576_) {
        when (event) {
            is GenEvent_576_.Load -> loadAll()
            is GenEvent_576_.Update -> save(event.model)
            is GenEvent_576_.Delete -> delete(event.id)
            is GenEvent_576_.Refresh -> loadAll()
            is GenEvent_576_.Search -> search(event.query)
            is GenEvent_576_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_576_.Loading; _state.value = GenState_576_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_576_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_576_.Success(searchUseCase(query)) } }
}
