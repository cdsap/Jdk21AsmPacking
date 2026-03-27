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

data class GenModel_935_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_935_ {
    data class Load(val id: Long) : GenEvent_935_()
    data class Update(val model: GenModel_935_) : GenEvent_935_()
    data class Delete(val id: Long) : GenEvent_935_()
    data object Refresh : GenEvent_935_()
    data class Search(val query: String) : GenEvent_935_()
    data class Filter(val predicate: String) : GenEvent_935_()
}

sealed class GenState_935_ {
    data object Idle : GenState_935_()
    data object Loading : GenState_935_()
    data class Success(val items: List<GenModel_935_>) : GenState_935_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_935_()
    data class Partial(val items: List<GenModel_935_>, val hasMore: Boolean) : GenState_935_()
}

interface GenRepository_935_ {
    suspend fun getAll(): List<GenModel_935_>
    suspend fun getById(id: Long): GenModel_935_?
    suspend fun save(model: GenModel_935_): GenModel_935_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_935_>
}

@Singleton
class GenRepositoryImpl_935_ @Inject constructor() : GenRepository_935_ {
    private val store = mutableMapOf<Long, GenModel_935_>()
    override suspend fun getAll(): List<GenModel_935_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_935_? = store[id]
    override suspend fun save(model: GenModel_935_): GenModel_935_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_935_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_935_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_935_ @Inject constructor(
    private val repository: GenRepositoryImpl_935_
) : GenUseCase_935_<Unit, List<GenModel_935_>> {
    override suspend fun invoke(params: Unit): List<GenModel_935_> = repository.getAll()
}

class GenSaveUseCase_935_ @Inject constructor(
    private val repository: GenRepositoryImpl_935_
) : GenUseCase_935_<GenModel_935_, GenModel_935_> {
    override suspend fun invoke(params: GenModel_935_): GenModel_935_ = repository.save(params)
}

class GenDeleteUseCase_935_ @Inject constructor(
    private val repository: GenRepositoryImpl_935_
) : GenUseCase_935_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_935_ @Inject constructor(
    private val repository: GenRepositoryImpl_935_
) : GenUseCase_935_<String, List<GenModel_935_>> {
    override suspend fun invoke(params: String): List<GenModel_935_> = repository.search(params)
}

abstract class GenMapper_935_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_935_ : GenMapper_935_<GenModel_935_, String>() {
    override fun map(input: GenModel_935_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_935_ : GenMapper_935_<String, GenModel_935_>() {
    override fun map(input: String): GenModel_935_ {
        val parts = input.split(":")
        return GenModel_935_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_935_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_935_,
    private val saveUseCase: GenSaveUseCase_935_,
    private val deleteUseCase: GenDeleteUseCase_935_,
    private val searchUseCase: GenSearchUseCase_935_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_935_>(GenState_935_.Idle)
    val state: StateFlow<GenState_935_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_935_) {
        when (event) {
            is GenEvent_935_.Load -> loadAll()
            is GenEvent_935_.Update -> save(event.model)
            is GenEvent_935_.Delete -> delete(event.id)
            is GenEvent_935_.Refresh -> loadAll()
            is GenEvent_935_.Search -> search(event.query)
            is GenEvent_935_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_935_.Loading; _state.value = GenState_935_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_935_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_935_.Success(searchUseCase(query)) } }
}
