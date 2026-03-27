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

data class GenModel_835_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_835_ {
    data class Load(val id: Long) : GenEvent_835_()
    data class Update(val model: GenModel_835_) : GenEvent_835_()
    data class Delete(val id: Long) : GenEvent_835_()
    data object Refresh : GenEvent_835_()
    data class Search(val query: String) : GenEvent_835_()
    data class Filter(val predicate: String) : GenEvent_835_()
}

sealed class GenState_835_ {
    data object Idle : GenState_835_()
    data object Loading : GenState_835_()
    data class Success(val items: List<GenModel_835_>) : GenState_835_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_835_()
    data class Partial(val items: List<GenModel_835_>, val hasMore: Boolean) : GenState_835_()
}

interface GenRepository_835_ {
    suspend fun getAll(): List<GenModel_835_>
    suspend fun getById(id: Long): GenModel_835_?
    suspend fun save(model: GenModel_835_): GenModel_835_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_835_>
}

@Singleton
class GenRepositoryImpl_835_ @Inject constructor() : GenRepository_835_ {
    private val store = mutableMapOf<Long, GenModel_835_>()
    override suspend fun getAll(): List<GenModel_835_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_835_? = store[id]
    override suspend fun save(model: GenModel_835_): GenModel_835_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_835_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_835_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_835_ @Inject constructor(
    private val repository: GenRepositoryImpl_835_
) : GenUseCase_835_<Unit, List<GenModel_835_>> {
    override suspend fun invoke(params: Unit): List<GenModel_835_> = repository.getAll()
}

class GenSaveUseCase_835_ @Inject constructor(
    private val repository: GenRepositoryImpl_835_
) : GenUseCase_835_<GenModel_835_, GenModel_835_> {
    override suspend fun invoke(params: GenModel_835_): GenModel_835_ = repository.save(params)
}

class GenDeleteUseCase_835_ @Inject constructor(
    private val repository: GenRepositoryImpl_835_
) : GenUseCase_835_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_835_ @Inject constructor(
    private val repository: GenRepositoryImpl_835_
) : GenUseCase_835_<String, List<GenModel_835_>> {
    override suspend fun invoke(params: String): List<GenModel_835_> = repository.search(params)
}

abstract class GenMapper_835_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_835_ : GenMapper_835_<GenModel_835_, String>() {
    override fun map(input: GenModel_835_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_835_ : GenMapper_835_<String, GenModel_835_>() {
    override fun map(input: String): GenModel_835_ {
        val parts = input.split(":")
        return GenModel_835_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_835_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_835_,
    private val saveUseCase: GenSaveUseCase_835_,
    private val deleteUseCase: GenDeleteUseCase_835_,
    private val searchUseCase: GenSearchUseCase_835_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_835_>(GenState_835_.Idle)
    val state: StateFlow<GenState_835_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_835_) {
        when (event) {
            is GenEvent_835_.Load -> loadAll()
            is GenEvent_835_.Update -> save(event.model)
            is GenEvent_835_.Delete -> delete(event.id)
            is GenEvent_835_.Refresh -> loadAll()
            is GenEvent_835_.Search -> search(event.query)
            is GenEvent_835_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_835_.Loading; _state.value = GenState_835_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_835_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_835_.Success(searchUseCase(query)) } }
}
