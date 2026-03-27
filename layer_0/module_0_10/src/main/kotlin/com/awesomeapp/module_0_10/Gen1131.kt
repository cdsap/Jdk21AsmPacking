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

data class GenModel_1131_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1131_ {
    data class Load(val id: Long) : GenEvent_1131_()
    data class Update(val model: GenModel_1131_) : GenEvent_1131_()
    data class Delete(val id: Long) : GenEvent_1131_()
    data object Refresh : GenEvent_1131_()
    data class Search(val query: String) : GenEvent_1131_()
    data class Filter(val predicate: String) : GenEvent_1131_()
}

sealed class GenState_1131_ {
    data object Idle : GenState_1131_()
    data object Loading : GenState_1131_()
    data class Success(val items: List<GenModel_1131_>) : GenState_1131_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1131_()
    data class Partial(val items: List<GenModel_1131_>, val hasMore: Boolean) : GenState_1131_()
}

interface GenRepository_1131_ {
    suspend fun getAll(): List<GenModel_1131_>
    suspend fun getById(id: Long): GenModel_1131_?
    suspend fun save(model: GenModel_1131_): GenModel_1131_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1131_>
}

@Singleton
class GenRepositoryImpl_1131_ @Inject constructor() : GenRepository_1131_ {
    private val store = mutableMapOf<Long, GenModel_1131_>()
    override suspend fun getAll(): List<GenModel_1131_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1131_? = store[id]
    override suspend fun save(model: GenModel_1131_): GenModel_1131_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1131_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1131_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1131_ @Inject constructor(
    private val repository: GenRepositoryImpl_1131_
) : GenUseCase_1131_<Unit, List<GenModel_1131_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1131_> = repository.getAll()
}

class GenSaveUseCase_1131_ @Inject constructor(
    private val repository: GenRepositoryImpl_1131_
) : GenUseCase_1131_<GenModel_1131_, GenModel_1131_> {
    override suspend fun invoke(params: GenModel_1131_): GenModel_1131_ = repository.save(params)
}

class GenDeleteUseCase_1131_ @Inject constructor(
    private val repository: GenRepositoryImpl_1131_
) : GenUseCase_1131_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1131_ @Inject constructor(
    private val repository: GenRepositoryImpl_1131_
) : GenUseCase_1131_<String, List<GenModel_1131_>> {
    override suspend fun invoke(params: String): List<GenModel_1131_> = repository.search(params)
}

abstract class GenMapper_1131_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1131_ : GenMapper_1131_<GenModel_1131_, String>() {
    override fun map(input: GenModel_1131_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1131_ : GenMapper_1131_<String, GenModel_1131_>() {
    override fun map(input: String): GenModel_1131_ {
        val parts = input.split(":")
        return GenModel_1131_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1131_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1131_,
    private val saveUseCase: GenSaveUseCase_1131_,
    private val deleteUseCase: GenDeleteUseCase_1131_,
    private val searchUseCase: GenSearchUseCase_1131_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1131_>(GenState_1131_.Idle)
    val state: StateFlow<GenState_1131_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1131_) {
        when (event) {
            is GenEvent_1131_.Load -> loadAll()
            is GenEvent_1131_.Update -> save(event.model)
            is GenEvent_1131_.Delete -> delete(event.id)
            is GenEvent_1131_.Refresh -> loadAll()
            is GenEvent_1131_.Search -> search(event.query)
            is GenEvent_1131_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1131_.Loading; _state.value = GenState_1131_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1131_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1131_.Success(searchUseCase(query)) } }
}
