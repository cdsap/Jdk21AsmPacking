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

data class GenModel_85_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_85_ {
    data class Load(val id: Long) : GenEvent_85_()
    data class Update(val model: GenModel_85_) : GenEvent_85_()
    data class Delete(val id: Long) : GenEvent_85_()
    data object Refresh : GenEvent_85_()
    data class Search(val query: String) : GenEvent_85_()
    data class Filter(val predicate: String) : GenEvent_85_()
}

sealed class GenState_85_ {
    data object Idle : GenState_85_()
    data object Loading : GenState_85_()
    data class Success(val items: List<GenModel_85_>) : GenState_85_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_85_()
    data class Partial(val items: List<GenModel_85_>, val hasMore: Boolean) : GenState_85_()
}

interface GenRepository_85_ {
    suspend fun getAll(): List<GenModel_85_>
    suspend fun getById(id: Long): GenModel_85_?
    suspend fun save(model: GenModel_85_): GenModel_85_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_85_>
}

@Singleton
class GenRepositoryImpl_85_ @Inject constructor() : GenRepository_85_ {
    private val store = mutableMapOf<Long, GenModel_85_>()
    override suspend fun getAll(): List<GenModel_85_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_85_? = store[id]
    override suspend fun save(model: GenModel_85_): GenModel_85_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_85_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_85_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_85_ @Inject constructor(
    private val repository: GenRepositoryImpl_85_
) : GenUseCase_85_<Unit, List<GenModel_85_>> {
    override suspend fun invoke(params: Unit): List<GenModel_85_> = repository.getAll()
}

class GenSaveUseCase_85_ @Inject constructor(
    private val repository: GenRepositoryImpl_85_
) : GenUseCase_85_<GenModel_85_, GenModel_85_> {
    override suspend fun invoke(params: GenModel_85_): GenModel_85_ = repository.save(params)
}

class GenDeleteUseCase_85_ @Inject constructor(
    private val repository: GenRepositoryImpl_85_
) : GenUseCase_85_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_85_ @Inject constructor(
    private val repository: GenRepositoryImpl_85_
) : GenUseCase_85_<String, List<GenModel_85_>> {
    override suspend fun invoke(params: String): List<GenModel_85_> = repository.search(params)
}

abstract class GenMapper_85_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_85_ : GenMapper_85_<GenModel_85_, String>() {
    override fun map(input: GenModel_85_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_85_ : GenMapper_85_<String, GenModel_85_>() {
    override fun map(input: String): GenModel_85_ {
        val parts = input.split(":")
        return GenModel_85_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_85_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_85_,
    private val saveUseCase: GenSaveUseCase_85_,
    private val deleteUseCase: GenDeleteUseCase_85_,
    private val searchUseCase: GenSearchUseCase_85_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_85_>(GenState_85_.Idle)
    val state: StateFlow<GenState_85_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_85_) {
        when (event) {
            is GenEvent_85_.Load -> loadAll()
            is GenEvent_85_.Update -> save(event.model)
            is GenEvent_85_.Delete -> delete(event.id)
            is GenEvent_85_.Refresh -> loadAll()
            is GenEvent_85_.Search -> search(event.query)
            is GenEvent_85_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_85_.Loading; _state.value = GenState_85_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_85_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_85_.Success(searchUseCase(query)) } }
}
