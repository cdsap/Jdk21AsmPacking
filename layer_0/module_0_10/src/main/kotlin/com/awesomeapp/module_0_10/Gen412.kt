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

data class GenModel_412_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_412_ {
    data class Load(val id: Long) : GenEvent_412_()
    data class Update(val model: GenModel_412_) : GenEvent_412_()
    data class Delete(val id: Long) : GenEvent_412_()
    data object Refresh : GenEvent_412_()
    data class Search(val query: String) : GenEvent_412_()
    data class Filter(val predicate: String) : GenEvent_412_()
}

sealed class GenState_412_ {
    data object Idle : GenState_412_()
    data object Loading : GenState_412_()
    data class Success(val items: List<GenModel_412_>) : GenState_412_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_412_()
    data class Partial(val items: List<GenModel_412_>, val hasMore: Boolean) : GenState_412_()
}

interface GenRepository_412_ {
    suspend fun getAll(): List<GenModel_412_>
    suspend fun getById(id: Long): GenModel_412_?
    suspend fun save(model: GenModel_412_): GenModel_412_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_412_>
}

@Singleton
class GenRepositoryImpl_412_ @Inject constructor() : GenRepository_412_ {
    private val store = mutableMapOf<Long, GenModel_412_>()
    override suspend fun getAll(): List<GenModel_412_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_412_? = store[id]
    override suspend fun save(model: GenModel_412_): GenModel_412_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_412_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_412_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_412_ @Inject constructor(
    private val repository: GenRepositoryImpl_412_
) : GenUseCase_412_<Unit, List<GenModel_412_>> {
    override suspend fun invoke(params: Unit): List<GenModel_412_> = repository.getAll()
}

class GenSaveUseCase_412_ @Inject constructor(
    private val repository: GenRepositoryImpl_412_
) : GenUseCase_412_<GenModel_412_, GenModel_412_> {
    override suspend fun invoke(params: GenModel_412_): GenModel_412_ = repository.save(params)
}

class GenDeleteUseCase_412_ @Inject constructor(
    private val repository: GenRepositoryImpl_412_
) : GenUseCase_412_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_412_ @Inject constructor(
    private val repository: GenRepositoryImpl_412_
) : GenUseCase_412_<String, List<GenModel_412_>> {
    override suspend fun invoke(params: String): List<GenModel_412_> = repository.search(params)
}

abstract class GenMapper_412_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_412_ : GenMapper_412_<GenModel_412_, String>() {
    override fun map(input: GenModel_412_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_412_ : GenMapper_412_<String, GenModel_412_>() {
    override fun map(input: String): GenModel_412_ {
        val parts = input.split(":")
        return GenModel_412_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_412_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_412_,
    private val saveUseCase: GenSaveUseCase_412_,
    private val deleteUseCase: GenDeleteUseCase_412_,
    private val searchUseCase: GenSearchUseCase_412_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_412_>(GenState_412_.Idle)
    val state: StateFlow<GenState_412_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_412_) {
        when (event) {
            is GenEvent_412_.Load -> loadAll()
            is GenEvent_412_.Update -> save(event.model)
            is GenEvent_412_.Delete -> delete(event.id)
            is GenEvent_412_.Refresh -> loadAll()
            is GenEvent_412_.Search -> search(event.query)
            is GenEvent_412_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_412_.Loading; _state.value = GenState_412_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_412_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_412_.Success(searchUseCase(query)) } }
}
