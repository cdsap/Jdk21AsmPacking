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

data class GenModel_3166_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3166_ {
    data class Load(val id: Long) : GenEvent_3166_()
    data class Update(val model: GenModel_3166_) : GenEvent_3166_()
    data class Delete(val id: Long) : GenEvent_3166_()
    data object Refresh : GenEvent_3166_()
    data class Search(val query: String) : GenEvent_3166_()
    data class Filter(val predicate: String) : GenEvent_3166_()
}

sealed class GenState_3166_ {
    data object Idle : GenState_3166_()
    data object Loading : GenState_3166_()
    data class Success(val items: List<GenModel_3166_>) : GenState_3166_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3166_()
    data class Partial(val items: List<GenModel_3166_>, val hasMore: Boolean) : GenState_3166_()
}

interface GenRepository_3166_ {
    suspend fun getAll(): List<GenModel_3166_>
    suspend fun getById(id: Long): GenModel_3166_?
    suspend fun save(model: GenModel_3166_): GenModel_3166_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3166_>
}

@Singleton
class GenRepositoryImpl_3166_ @Inject constructor() : GenRepository_3166_ {
    private val store = mutableMapOf<Long, GenModel_3166_>()
    override suspend fun getAll(): List<GenModel_3166_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3166_? = store[id]
    override suspend fun save(model: GenModel_3166_): GenModel_3166_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3166_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3166_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3166_ @Inject constructor(
    private val repository: GenRepositoryImpl_3166_
) : GenUseCase_3166_<Unit, List<GenModel_3166_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3166_> = repository.getAll()
}

class GenSaveUseCase_3166_ @Inject constructor(
    private val repository: GenRepositoryImpl_3166_
) : GenUseCase_3166_<GenModel_3166_, GenModel_3166_> {
    override suspend fun invoke(params: GenModel_3166_): GenModel_3166_ = repository.save(params)
}

class GenDeleteUseCase_3166_ @Inject constructor(
    private val repository: GenRepositoryImpl_3166_
) : GenUseCase_3166_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3166_ @Inject constructor(
    private val repository: GenRepositoryImpl_3166_
) : GenUseCase_3166_<String, List<GenModel_3166_>> {
    override suspend fun invoke(params: String): List<GenModel_3166_> = repository.search(params)
}

abstract class GenMapper_3166_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3166_ : GenMapper_3166_<GenModel_3166_, String>() {
    override fun map(input: GenModel_3166_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3166_ : GenMapper_3166_<String, GenModel_3166_>() {
    override fun map(input: String): GenModel_3166_ {
        val parts = input.split(":")
        return GenModel_3166_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3166_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3166_,
    private val saveUseCase: GenSaveUseCase_3166_,
    private val deleteUseCase: GenDeleteUseCase_3166_,
    private val searchUseCase: GenSearchUseCase_3166_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3166_>(GenState_3166_.Idle)
    val state: StateFlow<GenState_3166_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3166_) {
        when (event) {
            is GenEvent_3166_.Load -> loadAll()
            is GenEvent_3166_.Update -> save(event.model)
            is GenEvent_3166_.Delete -> delete(event.id)
            is GenEvent_3166_.Refresh -> loadAll()
            is GenEvent_3166_.Search -> search(event.query)
            is GenEvent_3166_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3166_.Loading; _state.value = GenState_3166_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3166_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3166_.Success(searchUseCase(query)) } }
}
