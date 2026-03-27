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

data class GenModel_53_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_53_ {
    data class Load(val id: Long) : GenEvent_53_()
    data class Update(val model: GenModel_53_) : GenEvent_53_()
    data class Delete(val id: Long) : GenEvent_53_()
    data object Refresh : GenEvent_53_()
    data class Search(val query: String) : GenEvent_53_()
    data class Filter(val predicate: String) : GenEvent_53_()
}

sealed class GenState_53_ {
    data object Idle : GenState_53_()
    data object Loading : GenState_53_()
    data class Success(val items: List<GenModel_53_>) : GenState_53_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_53_()
    data class Partial(val items: List<GenModel_53_>, val hasMore: Boolean) : GenState_53_()
}

interface GenRepository_53_ {
    suspend fun getAll(): List<GenModel_53_>
    suspend fun getById(id: Long): GenModel_53_?
    suspend fun save(model: GenModel_53_): GenModel_53_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_53_>
}

@Singleton
class GenRepositoryImpl_53_ @Inject constructor() : GenRepository_53_ {
    private val store = mutableMapOf<Long, GenModel_53_>()
    override suspend fun getAll(): List<GenModel_53_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_53_? = store[id]
    override suspend fun save(model: GenModel_53_): GenModel_53_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_53_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_53_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_53_ @Inject constructor(
    private val repository: GenRepositoryImpl_53_
) : GenUseCase_53_<Unit, List<GenModel_53_>> {
    override suspend fun invoke(params: Unit): List<GenModel_53_> = repository.getAll()
}

class GenSaveUseCase_53_ @Inject constructor(
    private val repository: GenRepositoryImpl_53_
) : GenUseCase_53_<GenModel_53_, GenModel_53_> {
    override suspend fun invoke(params: GenModel_53_): GenModel_53_ = repository.save(params)
}

class GenDeleteUseCase_53_ @Inject constructor(
    private val repository: GenRepositoryImpl_53_
) : GenUseCase_53_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_53_ @Inject constructor(
    private val repository: GenRepositoryImpl_53_
) : GenUseCase_53_<String, List<GenModel_53_>> {
    override suspend fun invoke(params: String): List<GenModel_53_> = repository.search(params)
}

abstract class GenMapper_53_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_53_ : GenMapper_53_<GenModel_53_, String>() {
    override fun map(input: GenModel_53_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_53_ : GenMapper_53_<String, GenModel_53_>() {
    override fun map(input: String): GenModel_53_ {
        val parts = input.split(":")
        return GenModel_53_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_53_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_53_,
    private val saveUseCase: GenSaveUseCase_53_,
    private val deleteUseCase: GenDeleteUseCase_53_,
    private val searchUseCase: GenSearchUseCase_53_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_53_>(GenState_53_.Idle)
    val state: StateFlow<GenState_53_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_53_) {
        when (event) {
            is GenEvent_53_.Load -> loadAll()
            is GenEvent_53_.Update -> save(event.model)
            is GenEvent_53_.Delete -> delete(event.id)
            is GenEvent_53_.Refresh -> loadAll()
            is GenEvent_53_.Search -> search(event.query)
            is GenEvent_53_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_53_.Loading; _state.value = GenState_53_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_53_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_53_.Success(searchUseCase(query)) } }
}
