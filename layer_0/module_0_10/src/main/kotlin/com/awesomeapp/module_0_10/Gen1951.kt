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

data class GenModel_1951_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1951_ {
    data class Load(val id: Long) : GenEvent_1951_()
    data class Update(val model: GenModel_1951_) : GenEvent_1951_()
    data class Delete(val id: Long) : GenEvent_1951_()
    data object Refresh : GenEvent_1951_()
    data class Search(val query: String) : GenEvent_1951_()
    data class Filter(val predicate: String) : GenEvent_1951_()
}

sealed class GenState_1951_ {
    data object Idle : GenState_1951_()
    data object Loading : GenState_1951_()
    data class Success(val items: List<GenModel_1951_>) : GenState_1951_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1951_()
    data class Partial(val items: List<GenModel_1951_>, val hasMore: Boolean) : GenState_1951_()
}

interface GenRepository_1951_ {
    suspend fun getAll(): List<GenModel_1951_>
    suspend fun getById(id: Long): GenModel_1951_?
    suspend fun save(model: GenModel_1951_): GenModel_1951_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1951_>
}

@Singleton
class GenRepositoryImpl_1951_ @Inject constructor() : GenRepository_1951_ {
    private val store = mutableMapOf<Long, GenModel_1951_>()
    override suspend fun getAll(): List<GenModel_1951_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1951_? = store[id]
    override suspend fun save(model: GenModel_1951_): GenModel_1951_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1951_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1951_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1951_ @Inject constructor(
    private val repository: GenRepositoryImpl_1951_
) : GenUseCase_1951_<Unit, List<GenModel_1951_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1951_> = repository.getAll()
}

class GenSaveUseCase_1951_ @Inject constructor(
    private val repository: GenRepositoryImpl_1951_
) : GenUseCase_1951_<GenModel_1951_, GenModel_1951_> {
    override suspend fun invoke(params: GenModel_1951_): GenModel_1951_ = repository.save(params)
}

class GenDeleteUseCase_1951_ @Inject constructor(
    private val repository: GenRepositoryImpl_1951_
) : GenUseCase_1951_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1951_ @Inject constructor(
    private val repository: GenRepositoryImpl_1951_
) : GenUseCase_1951_<String, List<GenModel_1951_>> {
    override suspend fun invoke(params: String): List<GenModel_1951_> = repository.search(params)
}

abstract class GenMapper_1951_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1951_ : GenMapper_1951_<GenModel_1951_, String>() {
    override fun map(input: GenModel_1951_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1951_ : GenMapper_1951_<String, GenModel_1951_>() {
    override fun map(input: String): GenModel_1951_ {
        val parts = input.split(":")
        return GenModel_1951_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1951_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1951_,
    private val saveUseCase: GenSaveUseCase_1951_,
    private val deleteUseCase: GenDeleteUseCase_1951_,
    private val searchUseCase: GenSearchUseCase_1951_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1951_>(GenState_1951_.Idle)
    val state: StateFlow<GenState_1951_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1951_) {
        when (event) {
            is GenEvent_1951_.Load -> loadAll()
            is GenEvent_1951_.Update -> save(event.model)
            is GenEvent_1951_.Delete -> delete(event.id)
            is GenEvent_1951_.Refresh -> loadAll()
            is GenEvent_1951_.Search -> search(event.query)
            is GenEvent_1951_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1951_.Loading; _state.value = GenState_1951_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1951_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1951_.Success(searchUseCase(query)) } }
}
