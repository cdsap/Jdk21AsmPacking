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

data class GenModel_280_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_280_ {
    data class Load(val id: Long) : GenEvent_280_()
    data class Update(val model: GenModel_280_) : GenEvent_280_()
    data class Delete(val id: Long) : GenEvent_280_()
    data object Refresh : GenEvent_280_()
    data class Search(val query: String) : GenEvent_280_()
    data class Filter(val predicate: String) : GenEvent_280_()
}

sealed class GenState_280_ {
    data object Idle : GenState_280_()
    data object Loading : GenState_280_()
    data class Success(val items: List<GenModel_280_>) : GenState_280_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_280_()
    data class Partial(val items: List<GenModel_280_>, val hasMore: Boolean) : GenState_280_()
}

interface GenRepository_280_ {
    suspend fun getAll(): List<GenModel_280_>
    suspend fun getById(id: Long): GenModel_280_?
    suspend fun save(model: GenModel_280_): GenModel_280_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_280_>
}

@Singleton
class GenRepositoryImpl_280_ @Inject constructor() : GenRepository_280_ {
    private val store = mutableMapOf<Long, GenModel_280_>()
    override suspend fun getAll(): List<GenModel_280_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_280_? = store[id]
    override suspend fun save(model: GenModel_280_): GenModel_280_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_280_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_280_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_280_ @Inject constructor(
    private val repository: GenRepositoryImpl_280_
) : GenUseCase_280_<Unit, List<GenModel_280_>> {
    override suspend fun invoke(params: Unit): List<GenModel_280_> = repository.getAll()
}

class GenSaveUseCase_280_ @Inject constructor(
    private val repository: GenRepositoryImpl_280_
) : GenUseCase_280_<GenModel_280_, GenModel_280_> {
    override suspend fun invoke(params: GenModel_280_): GenModel_280_ = repository.save(params)
}

class GenDeleteUseCase_280_ @Inject constructor(
    private val repository: GenRepositoryImpl_280_
) : GenUseCase_280_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_280_ @Inject constructor(
    private val repository: GenRepositoryImpl_280_
) : GenUseCase_280_<String, List<GenModel_280_>> {
    override suspend fun invoke(params: String): List<GenModel_280_> = repository.search(params)
}

abstract class GenMapper_280_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_280_ : GenMapper_280_<GenModel_280_, String>() {
    override fun map(input: GenModel_280_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_280_ : GenMapper_280_<String, GenModel_280_>() {
    override fun map(input: String): GenModel_280_ {
        val parts = input.split(":")
        return GenModel_280_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_280_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_280_,
    private val saveUseCase: GenSaveUseCase_280_,
    private val deleteUseCase: GenDeleteUseCase_280_,
    private val searchUseCase: GenSearchUseCase_280_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_280_>(GenState_280_.Idle)
    val state: StateFlow<GenState_280_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_280_) {
        when (event) {
            is GenEvent_280_.Load -> loadAll()
            is GenEvent_280_.Update -> save(event.model)
            is GenEvent_280_.Delete -> delete(event.id)
            is GenEvent_280_.Refresh -> loadAll()
            is GenEvent_280_.Search -> search(event.query)
            is GenEvent_280_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_280_.Loading; _state.value = GenState_280_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_280_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_280_.Success(searchUseCase(query)) } }
}
