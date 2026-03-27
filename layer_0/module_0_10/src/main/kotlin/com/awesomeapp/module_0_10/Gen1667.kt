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

data class GenModel_1667_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1667_ {
    data class Load(val id: Long) : GenEvent_1667_()
    data class Update(val model: GenModel_1667_) : GenEvent_1667_()
    data class Delete(val id: Long) : GenEvent_1667_()
    data object Refresh : GenEvent_1667_()
    data class Search(val query: String) : GenEvent_1667_()
    data class Filter(val predicate: String) : GenEvent_1667_()
}

sealed class GenState_1667_ {
    data object Idle : GenState_1667_()
    data object Loading : GenState_1667_()
    data class Success(val items: List<GenModel_1667_>) : GenState_1667_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1667_()
    data class Partial(val items: List<GenModel_1667_>, val hasMore: Boolean) : GenState_1667_()
}

interface GenRepository_1667_ {
    suspend fun getAll(): List<GenModel_1667_>
    suspend fun getById(id: Long): GenModel_1667_?
    suspend fun save(model: GenModel_1667_): GenModel_1667_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1667_>
}

@Singleton
class GenRepositoryImpl_1667_ @Inject constructor() : GenRepository_1667_ {
    private val store = mutableMapOf<Long, GenModel_1667_>()
    override suspend fun getAll(): List<GenModel_1667_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1667_? = store[id]
    override suspend fun save(model: GenModel_1667_): GenModel_1667_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1667_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1667_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1667_ @Inject constructor(
    private val repository: GenRepositoryImpl_1667_
) : GenUseCase_1667_<Unit, List<GenModel_1667_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1667_> = repository.getAll()
}

class GenSaveUseCase_1667_ @Inject constructor(
    private val repository: GenRepositoryImpl_1667_
) : GenUseCase_1667_<GenModel_1667_, GenModel_1667_> {
    override suspend fun invoke(params: GenModel_1667_): GenModel_1667_ = repository.save(params)
}

class GenDeleteUseCase_1667_ @Inject constructor(
    private val repository: GenRepositoryImpl_1667_
) : GenUseCase_1667_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1667_ @Inject constructor(
    private val repository: GenRepositoryImpl_1667_
) : GenUseCase_1667_<String, List<GenModel_1667_>> {
    override suspend fun invoke(params: String): List<GenModel_1667_> = repository.search(params)
}

abstract class GenMapper_1667_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1667_ : GenMapper_1667_<GenModel_1667_, String>() {
    override fun map(input: GenModel_1667_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1667_ : GenMapper_1667_<String, GenModel_1667_>() {
    override fun map(input: String): GenModel_1667_ {
        val parts = input.split(":")
        return GenModel_1667_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1667_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1667_,
    private val saveUseCase: GenSaveUseCase_1667_,
    private val deleteUseCase: GenDeleteUseCase_1667_,
    private val searchUseCase: GenSearchUseCase_1667_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1667_>(GenState_1667_.Idle)
    val state: StateFlow<GenState_1667_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1667_) {
        when (event) {
            is GenEvent_1667_.Load -> loadAll()
            is GenEvent_1667_.Update -> save(event.model)
            is GenEvent_1667_.Delete -> delete(event.id)
            is GenEvent_1667_.Refresh -> loadAll()
            is GenEvent_1667_.Search -> search(event.query)
            is GenEvent_1667_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1667_.Loading; _state.value = GenState_1667_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1667_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1667_.Success(searchUseCase(query)) } }
}
