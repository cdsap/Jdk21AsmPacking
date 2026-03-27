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

data class GenModel_1166_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1166_ {
    data class Load(val id: Long) : GenEvent_1166_()
    data class Update(val model: GenModel_1166_) : GenEvent_1166_()
    data class Delete(val id: Long) : GenEvent_1166_()
    data object Refresh : GenEvent_1166_()
    data class Search(val query: String) : GenEvent_1166_()
    data class Filter(val predicate: String) : GenEvent_1166_()
}

sealed class GenState_1166_ {
    data object Idle : GenState_1166_()
    data object Loading : GenState_1166_()
    data class Success(val items: List<GenModel_1166_>) : GenState_1166_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1166_()
    data class Partial(val items: List<GenModel_1166_>, val hasMore: Boolean) : GenState_1166_()
}

interface GenRepository_1166_ {
    suspend fun getAll(): List<GenModel_1166_>
    suspend fun getById(id: Long): GenModel_1166_?
    suspend fun save(model: GenModel_1166_): GenModel_1166_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1166_>
}

@Singleton
class GenRepositoryImpl_1166_ @Inject constructor() : GenRepository_1166_ {
    private val store = mutableMapOf<Long, GenModel_1166_>()
    override suspend fun getAll(): List<GenModel_1166_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1166_? = store[id]
    override suspend fun save(model: GenModel_1166_): GenModel_1166_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1166_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1166_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1166_ @Inject constructor(
    private val repository: GenRepositoryImpl_1166_
) : GenUseCase_1166_<Unit, List<GenModel_1166_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1166_> = repository.getAll()
}

class GenSaveUseCase_1166_ @Inject constructor(
    private val repository: GenRepositoryImpl_1166_
) : GenUseCase_1166_<GenModel_1166_, GenModel_1166_> {
    override suspend fun invoke(params: GenModel_1166_): GenModel_1166_ = repository.save(params)
}

class GenDeleteUseCase_1166_ @Inject constructor(
    private val repository: GenRepositoryImpl_1166_
) : GenUseCase_1166_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1166_ @Inject constructor(
    private val repository: GenRepositoryImpl_1166_
) : GenUseCase_1166_<String, List<GenModel_1166_>> {
    override suspend fun invoke(params: String): List<GenModel_1166_> = repository.search(params)
}

abstract class GenMapper_1166_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1166_ : GenMapper_1166_<GenModel_1166_, String>() {
    override fun map(input: GenModel_1166_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1166_ : GenMapper_1166_<String, GenModel_1166_>() {
    override fun map(input: String): GenModel_1166_ {
        val parts = input.split(":")
        return GenModel_1166_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1166_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1166_,
    private val saveUseCase: GenSaveUseCase_1166_,
    private val deleteUseCase: GenDeleteUseCase_1166_,
    private val searchUseCase: GenSearchUseCase_1166_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1166_>(GenState_1166_.Idle)
    val state: StateFlow<GenState_1166_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1166_) {
        when (event) {
            is GenEvent_1166_.Load -> loadAll()
            is GenEvent_1166_.Update -> save(event.model)
            is GenEvent_1166_.Delete -> delete(event.id)
            is GenEvent_1166_.Refresh -> loadAll()
            is GenEvent_1166_.Search -> search(event.query)
            is GenEvent_1166_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1166_.Loading; _state.value = GenState_1166_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1166_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1166_.Success(searchUseCase(query)) } }
}
