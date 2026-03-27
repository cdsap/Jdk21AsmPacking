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

data class GenModel_830_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_830_ {
    data class Load(val id: Long) : GenEvent_830_()
    data class Update(val model: GenModel_830_) : GenEvent_830_()
    data class Delete(val id: Long) : GenEvent_830_()
    data object Refresh : GenEvent_830_()
    data class Search(val query: String) : GenEvent_830_()
    data class Filter(val predicate: String) : GenEvent_830_()
}

sealed class GenState_830_ {
    data object Idle : GenState_830_()
    data object Loading : GenState_830_()
    data class Success(val items: List<GenModel_830_>) : GenState_830_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_830_()
    data class Partial(val items: List<GenModel_830_>, val hasMore: Boolean) : GenState_830_()
}

interface GenRepository_830_ {
    suspend fun getAll(): List<GenModel_830_>
    suspend fun getById(id: Long): GenModel_830_?
    suspend fun save(model: GenModel_830_): GenModel_830_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_830_>
}

@Singleton
class GenRepositoryImpl_830_ @Inject constructor() : GenRepository_830_ {
    private val store = mutableMapOf<Long, GenModel_830_>()
    override suspend fun getAll(): List<GenModel_830_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_830_? = store[id]
    override suspend fun save(model: GenModel_830_): GenModel_830_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_830_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_830_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_830_ @Inject constructor(
    private val repository: GenRepositoryImpl_830_
) : GenUseCase_830_<Unit, List<GenModel_830_>> {
    override suspend fun invoke(params: Unit): List<GenModel_830_> = repository.getAll()
}

class GenSaveUseCase_830_ @Inject constructor(
    private val repository: GenRepositoryImpl_830_
) : GenUseCase_830_<GenModel_830_, GenModel_830_> {
    override suspend fun invoke(params: GenModel_830_): GenModel_830_ = repository.save(params)
}

class GenDeleteUseCase_830_ @Inject constructor(
    private val repository: GenRepositoryImpl_830_
) : GenUseCase_830_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_830_ @Inject constructor(
    private val repository: GenRepositoryImpl_830_
) : GenUseCase_830_<String, List<GenModel_830_>> {
    override suspend fun invoke(params: String): List<GenModel_830_> = repository.search(params)
}

abstract class GenMapper_830_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_830_ : GenMapper_830_<GenModel_830_, String>() {
    override fun map(input: GenModel_830_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_830_ : GenMapper_830_<String, GenModel_830_>() {
    override fun map(input: String): GenModel_830_ {
        val parts = input.split(":")
        return GenModel_830_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_830_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_830_,
    private val saveUseCase: GenSaveUseCase_830_,
    private val deleteUseCase: GenDeleteUseCase_830_,
    private val searchUseCase: GenSearchUseCase_830_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_830_>(GenState_830_.Idle)
    val state: StateFlow<GenState_830_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_830_) {
        when (event) {
            is GenEvent_830_.Load -> loadAll()
            is GenEvent_830_.Update -> save(event.model)
            is GenEvent_830_.Delete -> delete(event.id)
            is GenEvent_830_.Refresh -> loadAll()
            is GenEvent_830_.Search -> search(event.query)
            is GenEvent_830_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_830_.Loading; _state.value = GenState_830_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_830_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_830_.Success(searchUseCase(query)) } }
}
