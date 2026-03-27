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

data class GenModel_244_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_244_ {
    data class Load(val id: Long) : GenEvent_244_()
    data class Update(val model: GenModel_244_) : GenEvent_244_()
    data class Delete(val id: Long) : GenEvent_244_()
    data object Refresh : GenEvent_244_()
    data class Search(val query: String) : GenEvent_244_()
    data class Filter(val predicate: String) : GenEvent_244_()
}

sealed class GenState_244_ {
    data object Idle : GenState_244_()
    data object Loading : GenState_244_()
    data class Success(val items: List<GenModel_244_>) : GenState_244_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_244_()
    data class Partial(val items: List<GenModel_244_>, val hasMore: Boolean) : GenState_244_()
}

interface GenRepository_244_ {
    suspend fun getAll(): List<GenModel_244_>
    suspend fun getById(id: Long): GenModel_244_?
    suspend fun save(model: GenModel_244_): GenModel_244_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_244_>
}

@Singleton
class GenRepositoryImpl_244_ @Inject constructor() : GenRepository_244_ {
    private val store = mutableMapOf<Long, GenModel_244_>()
    override suspend fun getAll(): List<GenModel_244_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_244_? = store[id]
    override suspend fun save(model: GenModel_244_): GenModel_244_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_244_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_244_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_244_ @Inject constructor(
    private val repository: GenRepositoryImpl_244_
) : GenUseCase_244_<Unit, List<GenModel_244_>> {
    override suspend fun invoke(params: Unit): List<GenModel_244_> = repository.getAll()
}

class GenSaveUseCase_244_ @Inject constructor(
    private val repository: GenRepositoryImpl_244_
) : GenUseCase_244_<GenModel_244_, GenModel_244_> {
    override suspend fun invoke(params: GenModel_244_): GenModel_244_ = repository.save(params)
}

class GenDeleteUseCase_244_ @Inject constructor(
    private val repository: GenRepositoryImpl_244_
) : GenUseCase_244_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_244_ @Inject constructor(
    private val repository: GenRepositoryImpl_244_
) : GenUseCase_244_<String, List<GenModel_244_>> {
    override suspend fun invoke(params: String): List<GenModel_244_> = repository.search(params)
}

abstract class GenMapper_244_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_244_ : GenMapper_244_<GenModel_244_, String>() {
    override fun map(input: GenModel_244_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_244_ : GenMapper_244_<String, GenModel_244_>() {
    override fun map(input: String): GenModel_244_ {
        val parts = input.split(":")
        return GenModel_244_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_244_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_244_,
    private val saveUseCase: GenSaveUseCase_244_,
    private val deleteUseCase: GenDeleteUseCase_244_,
    private val searchUseCase: GenSearchUseCase_244_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_244_>(GenState_244_.Idle)
    val state: StateFlow<GenState_244_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_244_) {
        when (event) {
            is GenEvent_244_.Load -> loadAll()
            is GenEvent_244_.Update -> save(event.model)
            is GenEvent_244_.Delete -> delete(event.id)
            is GenEvent_244_.Refresh -> loadAll()
            is GenEvent_244_.Search -> search(event.query)
            is GenEvent_244_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_244_.Loading; _state.value = GenState_244_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_244_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_244_.Success(searchUseCase(query)) } }
}
