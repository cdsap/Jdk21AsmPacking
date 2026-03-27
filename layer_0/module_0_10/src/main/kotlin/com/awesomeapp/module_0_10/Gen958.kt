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

data class GenModel_958_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_958_ {
    data class Load(val id: Long) : GenEvent_958_()
    data class Update(val model: GenModel_958_) : GenEvent_958_()
    data class Delete(val id: Long) : GenEvent_958_()
    data object Refresh : GenEvent_958_()
    data class Search(val query: String) : GenEvent_958_()
    data class Filter(val predicate: String) : GenEvent_958_()
}

sealed class GenState_958_ {
    data object Idle : GenState_958_()
    data object Loading : GenState_958_()
    data class Success(val items: List<GenModel_958_>) : GenState_958_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_958_()
    data class Partial(val items: List<GenModel_958_>, val hasMore: Boolean) : GenState_958_()
}

interface GenRepository_958_ {
    suspend fun getAll(): List<GenModel_958_>
    suspend fun getById(id: Long): GenModel_958_?
    suspend fun save(model: GenModel_958_): GenModel_958_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_958_>
}

@Singleton
class GenRepositoryImpl_958_ @Inject constructor() : GenRepository_958_ {
    private val store = mutableMapOf<Long, GenModel_958_>()
    override suspend fun getAll(): List<GenModel_958_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_958_? = store[id]
    override suspend fun save(model: GenModel_958_): GenModel_958_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_958_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_958_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_958_ @Inject constructor(
    private val repository: GenRepositoryImpl_958_
) : GenUseCase_958_<Unit, List<GenModel_958_>> {
    override suspend fun invoke(params: Unit): List<GenModel_958_> = repository.getAll()
}

class GenSaveUseCase_958_ @Inject constructor(
    private val repository: GenRepositoryImpl_958_
) : GenUseCase_958_<GenModel_958_, GenModel_958_> {
    override suspend fun invoke(params: GenModel_958_): GenModel_958_ = repository.save(params)
}

class GenDeleteUseCase_958_ @Inject constructor(
    private val repository: GenRepositoryImpl_958_
) : GenUseCase_958_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_958_ @Inject constructor(
    private val repository: GenRepositoryImpl_958_
) : GenUseCase_958_<String, List<GenModel_958_>> {
    override suspend fun invoke(params: String): List<GenModel_958_> = repository.search(params)
}

abstract class GenMapper_958_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_958_ : GenMapper_958_<GenModel_958_, String>() {
    override fun map(input: GenModel_958_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_958_ : GenMapper_958_<String, GenModel_958_>() {
    override fun map(input: String): GenModel_958_ {
        val parts = input.split(":")
        return GenModel_958_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_958_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_958_,
    private val saveUseCase: GenSaveUseCase_958_,
    private val deleteUseCase: GenDeleteUseCase_958_,
    private val searchUseCase: GenSearchUseCase_958_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_958_>(GenState_958_.Idle)
    val state: StateFlow<GenState_958_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_958_) {
        when (event) {
            is GenEvent_958_.Load -> loadAll()
            is GenEvent_958_.Update -> save(event.model)
            is GenEvent_958_.Delete -> delete(event.id)
            is GenEvent_958_.Refresh -> loadAll()
            is GenEvent_958_.Search -> search(event.query)
            is GenEvent_958_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_958_.Loading; _state.value = GenState_958_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_958_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_958_.Success(searchUseCase(query)) } }
}
