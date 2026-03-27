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

data class GenModel_2951_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2951_ {
    data class Load(val id: Long) : GenEvent_2951_()
    data class Update(val model: GenModel_2951_) : GenEvent_2951_()
    data class Delete(val id: Long) : GenEvent_2951_()
    data object Refresh : GenEvent_2951_()
    data class Search(val query: String) : GenEvent_2951_()
    data class Filter(val predicate: String) : GenEvent_2951_()
}

sealed class GenState_2951_ {
    data object Idle : GenState_2951_()
    data object Loading : GenState_2951_()
    data class Success(val items: List<GenModel_2951_>) : GenState_2951_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2951_()
    data class Partial(val items: List<GenModel_2951_>, val hasMore: Boolean) : GenState_2951_()
}

interface GenRepository_2951_ {
    suspend fun getAll(): List<GenModel_2951_>
    suspend fun getById(id: Long): GenModel_2951_?
    suspend fun save(model: GenModel_2951_): GenModel_2951_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2951_>
}

@Singleton
class GenRepositoryImpl_2951_ @Inject constructor() : GenRepository_2951_ {
    private val store = mutableMapOf<Long, GenModel_2951_>()
    override suspend fun getAll(): List<GenModel_2951_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2951_? = store[id]
    override suspend fun save(model: GenModel_2951_): GenModel_2951_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2951_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2951_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2951_ @Inject constructor(
    private val repository: GenRepositoryImpl_2951_
) : GenUseCase_2951_<Unit, List<GenModel_2951_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2951_> = repository.getAll()
}

class GenSaveUseCase_2951_ @Inject constructor(
    private val repository: GenRepositoryImpl_2951_
) : GenUseCase_2951_<GenModel_2951_, GenModel_2951_> {
    override suspend fun invoke(params: GenModel_2951_): GenModel_2951_ = repository.save(params)
}

class GenDeleteUseCase_2951_ @Inject constructor(
    private val repository: GenRepositoryImpl_2951_
) : GenUseCase_2951_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2951_ @Inject constructor(
    private val repository: GenRepositoryImpl_2951_
) : GenUseCase_2951_<String, List<GenModel_2951_>> {
    override suspend fun invoke(params: String): List<GenModel_2951_> = repository.search(params)
}

abstract class GenMapper_2951_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2951_ : GenMapper_2951_<GenModel_2951_, String>() {
    override fun map(input: GenModel_2951_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2951_ : GenMapper_2951_<String, GenModel_2951_>() {
    override fun map(input: String): GenModel_2951_ {
        val parts = input.split(":")
        return GenModel_2951_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2951_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2951_,
    private val saveUseCase: GenSaveUseCase_2951_,
    private val deleteUseCase: GenDeleteUseCase_2951_,
    private val searchUseCase: GenSearchUseCase_2951_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2951_>(GenState_2951_.Idle)
    val state: StateFlow<GenState_2951_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2951_) {
        when (event) {
            is GenEvent_2951_.Load -> loadAll()
            is GenEvent_2951_.Update -> save(event.model)
            is GenEvent_2951_.Delete -> delete(event.id)
            is GenEvent_2951_.Refresh -> loadAll()
            is GenEvent_2951_.Search -> search(event.query)
            is GenEvent_2951_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2951_.Loading; _state.value = GenState_2951_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2951_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2951_.Success(searchUseCase(query)) } }
}
