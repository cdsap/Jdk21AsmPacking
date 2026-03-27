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

data class GenModel_66_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_66_ {
    data class Load(val id: Long) : GenEvent_66_()
    data class Update(val model: GenModel_66_) : GenEvent_66_()
    data class Delete(val id: Long) : GenEvent_66_()
    data object Refresh : GenEvent_66_()
    data class Search(val query: String) : GenEvent_66_()
    data class Filter(val predicate: String) : GenEvent_66_()
}

sealed class GenState_66_ {
    data object Idle : GenState_66_()
    data object Loading : GenState_66_()
    data class Success(val items: List<GenModel_66_>) : GenState_66_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_66_()
    data class Partial(val items: List<GenModel_66_>, val hasMore: Boolean) : GenState_66_()
}

interface GenRepository_66_ {
    suspend fun getAll(): List<GenModel_66_>
    suspend fun getById(id: Long): GenModel_66_?
    suspend fun save(model: GenModel_66_): GenModel_66_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_66_>
}

@Singleton
class GenRepositoryImpl_66_ @Inject constructor() : GenRepository_66_ {
    private val store = mutableMapOf<Long, GenModel_66_>()
    override suspend fun getAll(): List<GenModel_66_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_66_? = store[id]
    override suspend fun save(model: GenModel_66_): GenModel_66_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_66_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_66_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_66_ @Inject constructor(
    private val repository: GenRepositoryImpl_66_
) : GenUseCase_66_<Unit, List<GenModel_66_>> {
    override suspend fun invoke(params: Unit): List<GenModel_66_> = repository.getAll()
}

class GenSaveUseCase_66_ @Inject constructor(
    private val repository: GenRepositoryImpl_66_
) : GenUseCase_66_<GenModel_66_, GenModel_66_> {
    override suspend fun invoke(params: GenModel_66_): GenModel_66_ = repository.save(params)
}

class GenDeleteUseCase_66_ @Inject constructor(
    private val repository: GenRepositoryImpl_66_
) : GenUseCase_66_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_66_ @Inject constructor(
    private val repository: GenRepositoryImpl_66_
) : GenUseCase_66_<String, List<GenModel_66_>> {
    override suspend fun invoke(params: String): List<GenModel_66_> = repository.search(params)
}

abstract class GenMapper_66_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_66_ : GenMapper_66_<GenModel_66_, String>() {
    override fun map(input: GenModel_66_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_66_ : GenMapper_66_<String, GenModel_66_>() {
    override fun map(input: String): GenModel_66_ {
        val parts = input.split(":")
        return GenModel_66_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_66_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_66_,
    private val saveUseCase: GenSaveUseCase_66_,
    private val deleteUseCase: GenDeleteUseCase_66_,
    private val searchUseCase: GenSearchUseCase_66_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_66_>(GenState_66_.Idle)
    val state: StateFlow<GenState_66_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_66_) {
        when (event) {
            is GenEvent_66_.Load -> loadAll()
            is GenEvent_66_.Update -> save(event.model)
            is GenEvent_66_.Delete -> delete(event.id)
            is GenEvent_66_.Refresh -> loadAll()
            is GenEvent_66_.Search -> search(event.query)
            is GenEvent_66_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_66_.Loading; _state.value = GenState_66_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_66_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_66_.Success(searchUseCase(query)) } }
}
