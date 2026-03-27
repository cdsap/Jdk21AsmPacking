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

data class GenModel_189_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_189_ {
    data class Load(val id: Long) : GenEvent_189_()
    data class Update(val model: GenModel_189_) : GenEvent_189_()
    data class Delete(val id: Long) : GenEvent_189_()
    data object Refresh : GenEvent_189_()
    data class Search(val query: String) : GenEvent_189_()
    data class Filter(val predicate: String) : GenEvent_189_()
}

sealed class GenState_189_ {
    data object Idle : GenState_189_()
    data object Loading : GenState_189_()
    data class Success(val items: List<GenModel_189_>) : GenState_189_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_189_()
    data class Partial(val items: List<GenModel_189_>, val hasMore: Boolean) : GenState_189_()
}

interface GenRepository_189_ {
    suspend fun getAll(): List<GenModel_189_>
    suspend fun getById(id: Long): GenModel_189_?
    suspend fun save(model: GenModel_189_): GenModel_189_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_189_>
}

@Singleton
class GenRepositoryImpl_189_ @Inject constructor() : GenRepository_189_ {
    private val store = mutableMapOf<Long, GenModel_189_>()
    override suspend fun getAll(): List<GenModel_189_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_189_? = store[id]
    override suspend fun save(model: GenModel_189_): GenModel_189_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_189_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_189_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_189_ @Inject constructor(
    private val repository: GenRepositoryImpl_189_
) : GenUseCase_189_<Unit, List<GenModel_189_>> {
    override suspend fun invoke(params: Unit): List<GenModel_189_> = repository.getAll()
}

class GenSaveUseCase_189_ @Inject constructor(
    private val repository: GenRepositoryImpl_189_
) : GenUseCase_189_<GenModel_189_, GenModel_189_> {
    override suspend fun invoke(params: GenModel_189_): GenModel_189_ = repository.save(params)
}

class GenDeleteUseCase_189_ @Inject constructor(
    private val repository: GenRepositoryImpl_189_
) : GenUseCase_189_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_189_ @Inject constructor(
    private val repository: GenRepositoryImpl_189_
) : GenUseCase_189_<String, List<GenModel_189_>> {
    override suspend fun invoke(params: String): List<GenModel_189_> = repository.search(params)
}

abstract class GenMapper_189_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_189_ : GenMapper_189_<GenModel_189_, String>() {
    override fun map(input: GenModel_189_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_189_ : GenMapper_189_<String, GenModel_189_>() {
    override fun map(input: String): GenModel_189_ {
        val parts = input.split(":")
        return GenModel_189_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_189_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_189_,
    private val saveUseCase: GenSaveUseCase_189_,
    private val deleteUseCase: GenDeleteUseCase_189_,
    private val searchUseCase: GenSearchUseCase_189_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_189_>(GenState_189_.Idle)
    val state: StateFlow<GenState_189_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_189_) {
        when (event) {
            is GenEvent_189_.Load -> loadAll()
            is GenEvent_189_.Update -> save(event.model)
            is GenEvent_189_.Delete -> delete(event.id)
            is GenEvent_189_.Refresh -> loadAll()
            is GenEvent_189_.Search -> search(event.query)
            is GenEvent_189_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_189_.Loading; _state.value = GenState_189_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_189_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_189_.Success(searchUseCase(query)) } }
}
