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

data class GenModel_1051_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1051_ {
    data class Load(val id: Long) : GenEvent_1051_()
    data class Update(val model: GenModel_1051_) : GenEvent_1051_()
    data class Delete(val id: Long) : GenEvent_1051_()
    data object Refresh : GenEvent_1051_()
    data class Search(val query: String) : GenEvent_1051_()
    data class Filter(val predicate: String) : GenEvent_1051_()
}

sealed class GenState_1051_ {
    data object Idle : GenState_1051_()
    data object Loading : GenState_1051_()
    data class Success(val items: List<GenModel_1051_>) : GenState_1051_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1051_()
    data class Partial(val items: List<GenModel_1051_>, val hasMore: Boolean) : GenState_1051_()
}

interface GenRepository_1051_ {
    suspend fun getAll(): List<GenModel_1051_>
    suspend fun getById(id: Long): GenModel_1051_?
    suspend fun save(model: GenModel_1051_): GenModel_1051_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1051_>
}

@Singleton
class GenRepositoryImpl_1051_ @Inject constructor() : GenRepository_1051_ {
    private val store = mutableMapOf<Long, GenModel_1051_>()
    override suspend fun getAll(): List<GenModel_1051_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1051_? = store[id]
    override suspend fun save(model: GenModel_1051_): GenModel_1051_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1051_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1051_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1051_ @Inject constructor(
    private val repository: GenRepositoryImpl_1051_
) : GenUseCase_1051_<Unit, List<GenModel_1051_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1051_> = repository.getAll()
}

class GenSaveUseCase_1051_ @Inject constructor(
    private val repository: GenRepositoryImpl_1051_
) : GenUseCase_1051_<GenModel_1051_, GenModel_1051_> {
    override suspend fun invoke(params: GenModel_1051_): GenModel_1051_ = repository.save(params)
}

class GenDeleteUseCase_1051_ @Inject constructor(
    private val repository: GenRepositoryImpl_1051_
) : GenUseCase_1051_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1051_ @Inject constructor(
    private val repository: GenRepositoryImpl_1051_
) : GenUseCase_1051_<String, List<GenModel_1051_>> {
    override suspend fun invoke(params: String): List<GenModel_1051_> = repository.search(params)
}

abstract class GenMapper_1051_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1051_ : GenMapper_1051_<GenModel_1051_, String>() {
    override fun map(input: GenModel_1051_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1051_ : GenMapper_1051_<String, GenModel_1051_>() {
    override fun map(input: String): GenModel_1051_ {
        val parts = input.split(":")
        return GenModel_1051_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1051_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1051_,
    private val saveUseCase: GenSaveUseCase_1051_,
    private val deleteUseCase: GenDeleteUseCase_1051_,
    private val searchUseCase: GenSearchUseCase_1051_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1051_>(GenState_1051_.Idle)
    val state: StateFlow<GenState_1051_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1051_) {
        when (event) {
            is GenEvent_1051_.Load -> loadAll()
            is GenEvent_1051_.Update -> save(event.model)
            is GenEvent_1051_.Delete -> delete(event.id)
            is GenEvent_1051_.Refresh -> loadAll()
            is GenEvent_1051_.Search -> search(event.query)
            is GenEvent_1051_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1051_.Loading; _state.value = GenState_1051_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1051_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1051_.Success(searchUseCase(query)) } }
}
