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

data class GenModel_2280_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2280_ {
    data class Load(val id: Long) : GenEvent_2280_()
    data class Update(val model: GenModel_2280_) : GenEvent_2280_()
    data class Delete(val id: Long) : GenEvent_2280_()
    data object Refresh : GenEvent_2280_()
    data class Search(val query: String) : GenEvent_2280_()
    data class Filter(val predicate: String) : GenEvent_2280_()
}

sealed class GenState_2280_ {
    data object Idle : GenState_2280_()
    data object Loading : GenState_2280_()
    data class Success(val items: List<GenModel_2280_>) : GenState_2280_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2280_()
    data class Partial(val items: List<GenModel_2280_>, val hasMore: Boolean) : GenState_2280_()
}

interface GenRepository_2280_ {
    suspend fun getAll(): List<GenModel_2280_>
    suspend fun getById(id: Long): GenModel_2280_?
    suspend fun save(model: GenModel_2280_): GenModel_2280_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2280_>
}

@Singleton
class GenRepositoryImpl_2280_ @Inject constructor() : GenRepository_2280_ {
    private val store = mutableMapOf<Long, GenModel_2280_>()
    override suspend fun getAll(): List<GenModel_2280_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2280_? = store[id]
    override suspend fun save(model: GenModel_2280_): GenModel_2280_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2280_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2280_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2280_ @Inject constructor(
    private val repository: GenRepositoryImpl_2280_
) : GenUseCase_2280_<Unit, List<GenModel_2280_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2280_> = repository.getAll()
}

class GenSaveUseCase_2280_ @Inject constructor(
    private val repository: GenRepositoryImpl_2280_
) : GenUseCase_2280_<GenModel_2280_, GenModel_2280_> {
    override suspend fun invoke(params: GenModel_2280_): GenModel_2280_ = repository.save(params)
}

class GenDeleteUseCase_2280_ @Inject constructor(
    private val repository: GenRepositoryImpl_2280_
) : GenUseCase_2280_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2280_ @Inject constructor(
    private val repository: GenRepositoryImpl_2280_
) : GenUseCase_2280_<String, List<GenModel_2280_>> {
    override suspend fun invoke(params: String): List<GenModel_2280_> = repository.search(params)
}

abstract class GenMapper_2280_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2280_ : GenMapper_2280_<GenModel_2280_, String>() {
    override fun map(input: GenModel_2280_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2280_ : GenMapper_2280_<String, GenModel_2280_>() {
    override fun map(input: String): GenModel_2280_ {
        val parts = input.split(":")
        return GenModel_2280_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2280_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2280_,
    private val saveUseCase: GenSaveUseCase_2280_,
    private val deleteUseCase: GenDeleteUseCase_2280_,
    private val searchUseCase: GenSearchUseCase_2280_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2280_>(GenState_2280_.Idle)
    val state: StateFlow<GenState_2280_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2280_) {
        when (event) {
            is GenEvent_2280_.Load -> loadAll()
            is GenEvent_2280_.Update -> save(event.model)
            is GenEvent_2280_.Delete -> delete(event.id)
            is GenEvent_2280_.Refresh -> loadAll()
            is GenEvent_2280_.Search -> search(event.query)
            is GenEvent_2280_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2280_.Loading; _state.value = GenState_2280_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2280_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2280_.Success(searchUseCase(query)) } }
}
