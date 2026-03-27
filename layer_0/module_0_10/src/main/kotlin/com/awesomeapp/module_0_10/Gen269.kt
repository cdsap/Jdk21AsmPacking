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

data class GenModel_269_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_269_ {
    data class Load(val id: Long) : GenEvent_269_()
    data class Update(val model: GenModel_269_) : GenEvent_269_()
    data class Delete(val id: Long) : GenEvent_269_()
    data object Refresh : GenEvent_269_()
    data class Search(val query: String) : GenEvent_269_()
    data class Filter(val predicate: String) : GenEvent_269_()
}

sealed class GenState_269_ {
    data object Idle : GenState_269_()
    data object Loading : GenState_269_()
    data class Success(val items: List<GenModel_269_>) : GenState_269_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_269_()
    data class Partial(val items: List<GenModel_269_>, val hasMore: Boolean) : GenState_269_()
}

interface GenRepository_269_ {
    suspend fun getAll(): List<GenModel_269_>
    suspend fun getById(id: Long): GenModel_269_?
    suspend fun save(model: GenModel_269_): GenModel_269_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_269_>
}

@Singleton
class GenRepositoryImpl_269_ @Inject constructor() : GenRepository_269_ {
    private val store = mutableMapOf<Long, GenModel_269_>()
    override suspend fun getAll(): List<GenModel_269_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_269_? = store[id]
    override suspend fun save(model: GenModel_269_): GenModel_269_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_269_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_269_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_269_ @Inject constructor(
    private val repository: GenRepositoryImpl_269_
) : GenUseCase_269_<Unit, List<GenModel_269_>> {
    override suspend fun invoke(params: Unit): List<GenModel_269_> = repository.getAll()
}

class GenSaveUseCase_269_ @Inject constructor(
    private val repository: GenRepositoryImpl_269_
) : GenUseCase_269_<GenModel_269_, GenModel_269_> {
    override suspend fun invoke(params: GenModel_269_): GenModel_269_ = repository.save(params)
}

class GenDeleteUseCase_269_ @Inject constructor(
    private val repository: GenRepositoryImpl_269_
) : GenUseCase_269_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_269_ @Inject constructor(
    private val repository: GenRepositoryImpl_269_
) : GenUseCase_269_<String, List<GenModel_269_>> {
    override suspend fun invoke(params: String): List<GenModel_269_> = repository.search(params)
}

abstract class GenMapper_269_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_269_ : GenMapper_269_<GenModel_269_, String>() {
    override fun map(input: GenModel_269_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_269_ : GenMapper_269_<String, GenModel_269_>() {
    override fun map(input: String): GenModel_269_ {
        val parts = input.split(":")
        return GenModel_269_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_269_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_269_,
    private val saveUseCase: GenSaveUseCase_269_,
    private val deleteUseCase: GenDeleteUseCase_269_,
    private val searchUseCase: GenSearchUseCase_269_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_269_>(GenState_269_.Idle)
    val state: StateFlow<GenState_269_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_269_) {
        when (event) {
            is GenEvent_269_.Load -> loadAll()
            is GenEvent_269_.Update -> save(event.model)
            is GenEvent_269_.Delete -> delete(event.id)
            is GenEvent_269_.Refresh -> loadAll()
            is GenEvent_269_.Search -> search(event.query)
            is GenEvent_269_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_269_.Loading; _state.value = GenState_269_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_269_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_269_.Success(searchUseCase(query)) } }
}
