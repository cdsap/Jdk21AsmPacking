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

data class GenModel_94_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_94_ {
    data class Load(val id: Long) : GenEvent_94_()
    data class Update(val model: GenModel_94_) : GenEvent_94_()
    data class Delete(val id: Long) : GenEvent_94_()
    data object Refresh : GenEvent_94_()
    data class Search(val query: String) : GenEvent_94_()
    data class Filter(val predicate: String) : GenEvent_94_()
}

sealed class GenState_94_ {
    data object Idle : GenState_94_()
    data object Loading : GenState_94_()
    data class Success(val items: List<GenModel_94_>) : GenState_94_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_94_()
    data class Partial(val items: List<GenModel_94_>, val hasMore: Boolean) : GenState_94_()
}

interface GenRepository_94_ {
    suspend fun getAll(): List<GenModel_94_>
    suspend fun getById(id: Long): GenModel_94_?
    suspend fun save(model: GenModel_94_): GenModel_94_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_94_>
}

@Singleton
class GenRepositoryImpl_94_ @Inject constructor() : GenRepository_94_ {
    private val store = mutableMapOf<Long, GenModel_94_>()
    override suspend fun getAll(): List<GenModel_94_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_94_? = store[id]
    override suspend fun save(model: GenModel_94_): GenModel_94_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_94_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_94_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_94_ @Inject constructor(
    private val repository: GenRepositoryImpl_94_
) : GenUseCase_94_<Unit, List<GenModel_94_>> {
    override suspend fun invoke(params: Unit): List<GenModel_94_> = repository.getAll()
}

class GenSaveUseCase_94_ @Inject constructor(
    private val repository: GenRepositoryImpl_94_
) : GenUseCase_94_<GenModel_94_, GenModel_94_> {
    override suspend fun invoke(params: GenModel_94_): GenModel_94_ = repository.save(params)
}

class GenDeleteUseCase_94_ @Inject constructor(
    private val repository: GenRepositoryImpl_94_
) : GenUseCase_94_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_94_ @Inject constructor(
    private val repository: GenRepositoryImpl_94_
) : GenUseCase_94_<String, List<GenModel_94_>> {
    override suspend fun invoke(params: String): List<GenModel_94_> = repository.search(params)
}

abstract class GenMapper_94_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_94_ : GenMapper_94_<GenModel_94_, String>() {
    override fun map(input: GenModel_94_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_94_ : GenMapper_94_<String, GenModel_94_>() {
    override fun map(input: String): GenModel_94_ {
        val parts = input.split(":")
        return GenModel_94_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_94_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_94_,
    private val saveUseCase: GenSaveUseCase_94_,
    private val deleteUseCase: GenDeleteUseCase_94_,
    private val searchUseCase: GenSearchUseCase_94_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_94_>(GenState_94_.Idle)
    val state: StateFlow<GenState_94_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_94_) {
        when (event) {
            is GenEvent_94_.Load -> loadAll()
            is GenEvent_94_.Update -> save(event.model)
            is GenEvent_94_.Delete -> delete(event.id)
            is GenEvent_94_.Refresh -> loadAll()
            is GenEvent_94_.Search -> search(event.query)
            is GenEvent_94_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_94_.Loading; _state.value = GenState_94_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_94_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_94_.Success(searchUseCase(query)) } }
}
