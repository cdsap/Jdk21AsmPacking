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

data class GenModel_2617_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2617_ {
    data class Load(val id: Long) : GenEvent_2617_()
    data class Update(val model: GenModel_2617_) : GenEvent_2617_()
    data class Delete(val id: Long) : GenEvent_2617_()
    data object Refresh : GenEvent_2617_()
    data class Search(val query: String) : GenEvent_2617_()
    data class Filter(val predicate: String) : GenEvent_2617_()
}

sealed class GenState_2617_ {
    data object Idle : GenState_2617_()
    data object Loading : GenState_2617_()
    data class Success(val items: List<GenModel_2617_>) : GenState_2617_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2617_()
    data class Partial(val items: List<GenModel_2617_>, val hasMore: Boolean) : GenState_2617_()
}

interface GenRepository_2617_ {
    suspend fun getAll(): List<GenModel_2617_>
    suspend fun getById(id: Long): GenModel_2617_?
    suspend fun save(model: GenModel_2617_): GenModel_2617_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2617_>
}

@Singleton
class GenRepositoryImpl_2617_ @Inject constructor() : GenRepository_2617_ {
    private val store = mutableMapOf<Long, GenModel_2617_>()
    override suspend fun getAll(): List<GenModel_2617_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2617_? = store[id]
    override suspend fun save(model: GenModel_2617_): GenModel_2617_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2617_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2617_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2617_ @Inject constructor(
    private val repository: GenRepositoryImpl_2617_
) : GenUseCase_2617_<Unit, List<GenModel_2617_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2617_> = repository.getAll()
}

class GenSaveUseCase_2617_ @Inject constructor(
    private val repository: GenRepositoryImpl_2617_
) : GenUseCase_2617_<GenModel_2617_, GenModel_2617_> {
    override suspend fun invoke(params: GenModel_2617_): GenModel_2617_ = repository.save(params)
}

class GenDeleteUseCase_2617_ @Inject constructor(
    private val repository: GenRepositoryImpl_2617_
) : GenUseCase_2617_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2617_ @Inject constructor(
    private val repository: GenRepositoryImpl_2617_
) : GenUseCase_2617_<String, List<GenModel_2617_>> {
    override suspend fun invoke(params: String): List<GenModel_2617_> = repository.search(params)
}

abstract class GenMapper_2617_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2617_ : GenMapper_2617_<GenModel_2617_, String>() {
    override fun map(input: GenModel_2617_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2617_ : GenMapper_2617_<String, GenModel_2617_>() {
    override fun map(input: String): GenModel_2617_ {
        val parts = input.split(":")
        return GenModel_2617_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2617_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2617_,
    private val saveUseCase: GenSaveUseCase_2617_,
    private val deleteUseCase: GenDeleteUseCase_2617_,
    private val searchUseCase: GenSearchUseCase_2617_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2617_>(GenState_2617_.Idle)
    val state: StateFlow<GenState_2617_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2617_) {
        when (event) {
            is GenEvent_2617_.Load -> loadAll()
            is GenEvent_2617_.Update -> save(event.model)
            is GenEvent_2617_.Delete -> delete(event.id)
            is GenEvent_2617_.Refresh -> loadAll()
            is GenEvent_2617_.Search -> search(event.query)
            is GenEvent_2617_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2617_.Loading; _state.value = GenState_2617_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2617_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2617_.Success(searchUseCase(query)) } }
}
