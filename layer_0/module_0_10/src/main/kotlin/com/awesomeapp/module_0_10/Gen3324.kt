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

data class GenModel_3324_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3324_ {
    data class Load(val id: Long) : GenEvent_3324_()
    data class Update(val model: GenModel_3324_) : GenEvent_3324_()
    data class Delete(val id: Long) : GenEvent_3324_()
    data object Refresh : GenEvent_3324_()
    data class Search(val query: String) : GenEvent_3324_()
    data class Filter(val predicate: String) : GenEvent_3324_()
}

sealed class GenState_3324_ {
    data object Idle : GenState_3324_()
    data object Loading : GenState_3324_()
    data class Success(val items: List<GenModel_3324_>) : GenState_3324_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3324_()
    data class Partial(val items: List<GenModel_3324_>, val hasMore: Boolean) : GenState_3324_()
}

interface GenRepository_3324_ {
    suspend fun getAll(): List<GenModel_3324_>
    suspend fun getById(id: Long): GenModel_3324_?
    suspend fun save(model: GenModel_3324_): GenModel_3324_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3324_>
}

@Singleton
class GenRepositoryImpl_3324_ @Inject constructor() : GenRepository_3324_ {
    private val store = mutableMapOf<Long, GenModel_3324_>()
    override suspend fun getAll(): List<GenModel_3324_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3324_? = store[id]
    override suspend fun save(model: GenModel_3324_): GenModel_3324_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3324_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3324_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3324_ @Inject constructor(
    private val repository: GenRepositoryImpl_3324_
) : GenUseCase_3324_<Unit, List<GenModel_3324_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3324_> = repository.getAll()
}

class GenSaveUseCase_3324_ @Inject constructor(
    private val repository: GenRepositoryImpl_3324_
) : GenUseCase_3324_<GenModel_3324_, GenModel_3324_> {
    override suspend fun invoke(params: GenModel_3324_): GenModel_3324_ = repository.save(params)
}

class GenDeleteUseCase_3324_ @Inject constructor(
    private val repository: GenRepositoryImpl_3324_
) : GenUseCase_3324_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3324_ @Inject constructor(
    private val repository: GenRepositoryImpl_3324_
) : GenUseCase_3324_<String, List<GenModel_3324_>> {
    override suspend fun invoke(params: String): List<GenModel_3324_> = repository.search(params)
}

abstract class GenMapper_3324_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3324_ : GenMapper_3324_<GenModel_3324_, String>() {
    override fun map(input: GenModel_3324_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3324_ : GenMapper_3324_<String, GenModel_3324_>() {
    override fun map(input: String): GenModel_3324_ {
        val parts = input.split(":")
        return GenModel_3324_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3324_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3324_,
    private val saveUseCase: GenSaveUseCase_3324_,
    private val deleteUseCase: GenDeleteUseCase_3324_,
    private val searchUseCase: GenSearchUseCase_3324_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3324_>(GenState_3324_.Idle)
    val state: StateFlow<GenState_3324_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3324_) {
        when (event) {
            is GenEvent_3324_.Load -> loadAll()
            is GenEvent_3324_.Update -> save(event.model)
            is GenEvent_3324_.Delete -> delete(event.id)
            is GenEvent_3324_.Refresh -> loadAll()
            is GenEvent_3324_.Search -> search(event.query)
            is GenEvent_3324_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3324_.Loading; _state.value = GenState_3324_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3324_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3324_.Success(searchUseCase(query)) } }
}
