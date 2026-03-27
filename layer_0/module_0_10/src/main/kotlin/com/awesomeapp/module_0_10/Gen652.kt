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

data class GenModel_652_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_652_ {
    data class Load(val id: Long) : GenEvent_652_()
    data class Update(val model: GenModel_652_) : GenEvent_652_()
    data class Delete(val id: Long) : GenEvent_652_()
    data object Refresh : GenEvent_652_()
    data class Search(val query: String) : GenEvent_652_()
    data class Filter(val predicate: String) : GenEvent_652_()
}

sealed class GenState_652_ {
    data object Idle : GenState_652_()
    data object Loading : GenState_652_()
    data class Success(val items: List<GenModel_652_>) : GenState_652_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_652_()
    data class Partial(val items: List<GenModel_652_>, val hasMore: Boolean) : GenState_652_()
}

interface GenRepository_652_ {
    suspend fun getAll(): List<GenModel_652_>
    suspend fun getById(id: Long): GenModel_652_?
    suspend fun save(model: GenModel_652_): GenModel_652_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_652_>
}

@Singleton
class GenRepositoryImpl_652_ @Inject constructor() : GenRepository_652_ {
    private val store = mutableMapOf<Long, GenModel_652_>()
    override suspend fun getAll(): List<GenModel_652_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_652_? = store[id]
    override suspend fun save(model: GenModel_652_): GenModel_652_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_652_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_652_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_652_ @Inject constructor(
    private val repository: GenRepositoryImpl_652_
) : GenUseCase_652_<Unit, List<GenModel_652_>> {
    override suspend fun invoke(params: Unit): List<GenModel_652_> = repository.getAll()
}

class GenSaveUseCase_652_ @Inject constructor(
    private val repository: GenRepositoryImpl_652_
) : GenUseCase_652_<GenModel_652_, GenModel_652_> {
    override suspend fun invoke(params: GenModel_652_): GenModel_652_ = repository.save(params)
}

class GenDeleteUseCase_652_ @Inject constructor(
    private val repository: GenRepositoryImpl_652_
) : GenUseCase_652_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_652_ @Inject constructor(
    private val repository: GenRepositoryImpl_652_
) : GenUseCase_652_<String, List<GenModel_652_>> {
    override suspend fun invoke(params: String): List<GenModel_652_> = repository.search(params)
}

abstract class GenMapper_652_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_652_ : GenMapper_652_<GenModel_652_, String>() {
    override fun map(input: GenModel_652_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_652_ : GenMapper_652_<String, GenModel_652_>() {
    override fun map(input: String): GenModel_652_ {
        val parts = input.split(":")
        return GenModel_652_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_652_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_652_,
    private val saveUseCase: GenSaveUseCase_652_,
    private val deleteUseCase: GenDeleteUseCase_652_,
    private val searchUseCase: GenSearchUseCase_652_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_652_>(GenState_652_.Idle)
    val state: StateFlow<GenState_652_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_652_) {
        when (event) {
            is GenEvent_652_.Load -> loadAll()
            is GenEvent_652_.Update -> save(event.model)
            is GenEvent_652_.Delete -> delete(event.id)
            is GenEvent_652_.Refresh -> loadAll()
            is GenEvent_652_.Search -> search(event.query)
            is GenEvent_652_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_652_.Loading; _state.value = GenState_652_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_652_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_652_.Success(searchUseCase(query)) } }
}
