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

data class GenModel_550_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_550_ {
    data class Load(val id: Long) : GenEvent_550_()
    data class Update(val model: GenModel_550_) : GenEvent_550_()
    data class Delete(val id: Long) : GenEvent_550_()
    data object Refresh : GenEvent_550_()
    data class Search(val query: String) : GenEvent_550_()
    data class Filter(val predicate: String) : GenEvent_550_()
}

sealed class GenState_550_ {
    data object Idle : GenState_550_()
    data object Loading : GenState_550_()
    data class Success(val items: List<GenModel_550_>) : GenState_550_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_550_()
    data class Partial(val items: List<GenModel_550_>, val hasMore: Boolean) : GenState_550_()
}

interface GenRepository_550_ {
    suspend fun getAll(): List<GenModel_550_>
    suspend fun getById(id: Long): GenModel_550_?
    suspend fun save(model: GenModel_550_): GenModel_550_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_550_>
}

@Singleton
class GenRepositoryImpl_550_ @Inject constructor() : GenRepository_550_ {
    private val store = mutableMapOf<Long, GenModel_550_>()
    override suspend fun getAll(): List<GenModel_550_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_550_? = store[id]
    override suspend fun save(model: GenModel_550_): GenModel_550_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_550_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_550_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_550_ @Inject constructor(
    private val repository: GenRepositoryImpl_550_
) : GenUseCase_550_<Unit, List<GenModel_550_>> {
    override suspend fun invoke(params: Unit): List<GenModel_550_> = repository.getAll()
}

class GenSaveUseCase_550_ @Inject constructor(
    private val repository: GenRepositoryImpl_550_
) : GenUseCase_550_<GenModel_550_, GenModel_550_> {
    override suspend fun invoke(params: GenModel_550_): GenModel_550_ = repository.save(params)
}

class GenDeleteUseCase_550_ @Inject constructor(
    private val repository: GenRepositoryImpl_550_
) : GenUseCase_550_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_550_ @Inject constructor(
    private val repository: GenRepositoryImpl_550_
) : GenUseCase_550_<String, List<GenModel_550_>> {
    override suspend fun invoke(params: String): List<GenModel_550_> = repository.search(params)
}

abstract class GenMapper_550_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_550_ : GenMapper_550_<GenModel_550_, String>() {
    override fun map(input: GenModel_550_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_550_ : GenMapper_550_<String, GenModel_550_>() {
    override fun map(input: String): GenModel_550_ {
        val parts = input.split(":")
        return GenModel_550_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_550_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_550_,
    private val saveUseCase: GenSaveUseCase_550_,
    private val deleteUseCase: GenDeleteUseCase_550_,
    private val searchUseCase: GenSearchUseCase_550_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_550_>(GenState_550_.Idle)
    val state: StateFlow<GenState_550_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_550_) {
        when (event) {
            is GenEvent_550_.Load -> loadAll()
            is GenEvent_550_.Update -> save(event.model)
            is GenEvent_550_.Delete -> delete(event.id)
            is GenEvent_550_.Refresh -> loadAll()
            is GenEvent_550_.Search -> search(event.query)
            is GenEvent_550_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_550_.Loading; _state.value = GenState_550_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_550_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_550_.Success(searchUseCase(query)) } }
}
