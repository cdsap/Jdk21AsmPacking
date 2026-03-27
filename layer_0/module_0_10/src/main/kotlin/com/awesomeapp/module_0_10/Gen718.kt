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

data class GenModel_718_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_718_ {
    data class Load(val id: Long) : GenEvent_718_()
    data class Update(val model: GenModel_718_) : GenEvent_718_()
    data class Delete(val id: Long) : GenEvent_718_()
    data object Refresh : GenEvent_718_()
    data class Search(val query: String) : GenEvent_718_()
    data class Filter(val predicate: String) : GenEvent_718_()
}

sealed class GenState_718_ {
    data object Idle : GenState_718_()
    data object Loading : GenState_718_()
    data class Success(val items: List<GenModel_718_>) : GenState_718_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_718_()
    data class Partial(val items: List<GenModel_718_>, val hasMore: Boolean) : GenState_718_()
}

interface GenRepository_718_ {
    suspend fun getAll(): List<GenModel_718_>
    suspend fun getById(id: Long): GenModel_718_?
    suspend fun save(model: GenModel_718_): GenModel_718_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_718_>
}

@Singleton
class GenRepositoryImpl_718_ @Inject constructor() : GenRepository_718_ {
    private val store = mutableMapOf<Long, GenModel_718_>()
    override suspend fun getAll(): List<GenModel_718_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_718_? = store[id]
    override suspend fun save(model: GenModel_718_): GenModel_718_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_718_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_718_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_718_ @Inject constructor(
    private val repository: GenRepositoryImpl_718_
) : GenUseCase_718_<Unit, List<GenModel_718_>> {
    override suspend fun invoke(params: Unit): List<GenModel_718_> = repository.getAll()
}

class GenSaveUseCase_718_ @Inject constructor(
    private val repository: GenRepositoryImpl_718_
) : GenUseCase_718_<GenModel_718_, GenModel_718_> {
    override suspend fun invoke(params: GenModel_718_): GenModel_718_ = repository.save(params)
}

class GenDeleteUseCase_718_ @Inject constructor(
    private val repository: GenRepositoryImpl_718_
) : GenUseCase_718_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_718_ @Inject constructor(
    private val repository: GenRepositoryImpl_718_
) : GenUseCase_718_<String, List<GenModel_718_>> {
    override suspend fun invoke(params: String): List<GenModel_718_> = repository.search(params)
}

abstract class GenMapper_718_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_718_ : GenMapper_718_<GenModel_718_, String>() {
    override fun map(input: GenModel_718_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_718_ : GenMapper_718_<String, GenModel_718_>() {
    override fun map(input: String): GenModel_718_ {
        val parts = input.split(":")
        return GenModel_718_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_718_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_718_,
    private val saveUseCase: GenSaveUseCase_718_,
    private val deleteUseCase: GenDeleteUseCase_718_,
    private val searchUseCase: GenSearchUseCase_718_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_718_>(GenState_718_.Idle)
    val state: StateFlow<GenState_718_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_718_) {
        when (event) {
            is GenEvent_718_.Load -> loadAll()
            is GenEvent_718_.Update -> save(event.model)
            is GenEvent_718_.Delete -> delete(event.id)
            is GenEvent_718_.Refresh -> loadAll()
            is GenEvent_718_.Search -> search(event.query)
            is GenEvent_718_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_718_.Loading; _state.value = GenState_718_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_718_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_718_.Success(searchUseCase(query)) } }
}
