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

data class GenModel_1233_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1233_ {
    data class Load(val id: Long) : GenEvent_1233_()
    data class Update(val model: GenModel_1233_) : GenEvent_1233_()
    data class Delete(val id: Long) : GenEvent_1233_()
    data object Refresh : GenEvent_1233_()
    data class Search(val query: String) : GenEvent_1233_()
    data class Filter(val predicate: String) : GenEvent_1233_()
}

sealed class GenState_1233_ {
    data object Idle : GenState_1233_()
    data object Loading : GenState_1233_()
    data class Success(val items: List<GenModel_1233_>) : GenState_1233_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1233_()
    data class Partial(val items: List<GenModel_1233_>, val hasMore: Boolean) : GenState_1233_()
}

interface GenRepository_1233_ {
    suspend fun getAll(): List<GenModel_1233_>
    suspend fun getById(id: Long): GenModel_1233_?
    suspend fun save(model: GenModel_1233_): GenModel_1233_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1233_>
}

@Singleton
class GenRepositoryImpl_1233_ @Inject constructor() : GenRepository_1233_ {
    private val store = mutableMapOf<Long, GenModel_1233_>()
    override suspend fun getAll(): List<GenModel_1233_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1233_? = store[id]
    override suspend fun save(model: GenModel_1233_): GenModel_1233_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1233_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1233_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1233_ @Inject constructor(
    private val repository: GenRepositoryImpl_1233_
) : GenUseCase_1233_<Unit, List<GenModel_1233_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1233_> = repository.getAll()
}

class GenSaveUseCase_1233_ @Inject constructor(
    private val repository: GenRepositoryImpl_1233_
) : GenUseCase_1233_<GenModel_1233_, GenModel_1233_> {
    override suspend fun invoke(params: GenModel_1233_): GenModel_1233_ = repository.save(params)
}

class GenDeleteUseCase_1233_ @Inject constructor(
    private val repository: GenRepositoryImpl_1233_
) : GenUseCase_1233_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1233_ @Inject constructor(
    private val repository: GenRepositoryImpl_1233_
) : GenUseCase_1233_<String, List<GenModel_1233_>> {
    override suspend fun invoke(params: String): List<GenModel_1233_> = repository.search(params)
}

abstract class GenMapper_1233_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1233_ : GenMapper_1233_<GenModel_1233_, String>() {
    override fun map(input: GenModel_1233_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1233_ : GenMapper_1233_<String, GenModel_1233_>() {
    override fun map(input: String): GenModel_1233_ {
        val parts = input.split(":")
        return GenModel_1233_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1233_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1233_,
    private val saveUseCase: GenSaveUseCase_1233_,
    private val deleteUseCase: GenDeleteUseCase_1233_,
    private val searchUseCase: GenSearchUseCase_1233_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1233_>(GenState_1233_.Idle)
    val state: StateFlow<GenState_1233_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1233_) {
        when (event) {
            is GenEvent_1233_.Load -> loadAll()
            is GenEvent_1233_.Update -> save(event.model)
            is GenEvent_1233_.Delete -> delete(event.id)
            is GenEvent_1233_.Refresh -> loadAll()
            is GenEvent_1233_.Search -> search(event.query)
            is GenEvent_1233_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1233_.Loading; _state.value = GenState_1233_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1233_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1233_.Success(searchUseCase(query)) } }
}
