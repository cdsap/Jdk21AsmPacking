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

data class GenModel_388_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_388_ {
    data class Load(val id: Long) : GenEvent_388_()
    data class Update(val model: GenModel_388_) : GenEvent_388_()
    data class Delete(val id: Long) : GenEvent_388_()
    data object Refresh : GenEvent_388_()
    data class Search(val query: String) : GenEvent_388_()
    data class Filter(val predicate: String) : GenEvent_388_()
}

sealed class GenState_388_ {
    data object Idle : GenState_388_()
    data object Loading : GenState_388_()
    data class Success(val items: List<GenModel_388_>) : GenState_388_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_388_()
    data class Partial(val items: List<GenModel_388_>, val hasMore: Boolean) : GenState_388_()
}

interface GenRepository_388_ {
    suspend fun getAll(): List<GenModel_388_>
    suspend fun getById(id: Long): GenModel_388_?
    suspend fun save(model: GenModel_388_): GenModel_388_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_388_>
}

@Singleton
class GenRepositoryImpl_388_ @Inject constructor() : GenRepository_388_ {
    private val store = mutableMapOf<Long, GenModel_388_>()
    override suspend fun getAll(): List<GenModel_388_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_388_? = store[id]
    override suspend fun save(model: GenModel_388_): GenModel_388_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_388_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_388_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_388_ @Inject constructor(
    private val repository: GenRepositoryImpl_388_
) : GenUseCase_388_<Unit, List<GenModel_388_>> {
    override suspend fun invoke(params: Unit): List<GenModel_388_> = repository.getAll()
}

class GenSaveUseCase_388_ @Inject constructor(
    private val repository: GenRepositoryImpl_388_
) : GenUseCase_388_<GenModel_388_, GenModel_388_> {
    override suspend fun invoke(params: GenModel_388_): GenModel_388_ = repository.save(params)
}

class GenDeleteUseCase_388_ @Inject constructor(
    private val repository: GenRepositoryImpl_388_
) : GenUseCase_388_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_388_ @Inject constructor(
    private val repository: GenRepositoryImpl_388_
) : GenUseCase_388_<String, List<GenModel_388_>> {
    override suspend fun invoke(params: String): List<GenModel_388_> = repository.search(params)
}

abstract class GenMapper_388_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_388_ : GenMapper_388_<GenModel_388_, String>() {
    override fun map(input: GenModel_388_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_388_ : GenMapper_388_<String, GenModel_388_>() {
    override fun map(input: String): GenModel_388_ {
        val parts = input.split(":")
        return GenModel_388_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_388_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_388_,
    private val saveUseCase: GenSaveUseCase_388_,
    private val deleteUseCase: GenDeleteUseCase_388_,
    private val searchUseCase: GenSearchUseCase_388_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_388_>(GenState_388_.Idle)
    val state: StateFlow<GenState_388_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_388_) {
        when (event) {
            is GenEvent_388_.Load -> loadAll()
            is GenEvent_388_.Update -> save(event.model)
            is GenEvent_388_.Delete -> delete(event.id)
            is GenEvent_388_.Refresh -> loadAll()
            is GenEvent_388_.Search -> search(event.query)
            is GenEvent_388_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_388_.Loading; _state.value = GenState_388_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_388_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_388_.Success(searchUseCase(query)) } }
}
