package io.github.vivitoto.vanga.db.repository

import kotlinx.coroutines.flow.Flow
import io.github.vivitoto.vanga.db.SettingsStateWrapper
import io.github.vivitoto.vanga.homefilters.HomeScreenFilter
import io.github.vivitoto.vanga.homefilters.HomeScreenFilterRepository

class HomeScreenFilterRepositoryWrapper(
    private val wrapper: SettingsStateWrapper<List<HomeScreenFilter>>,
) : HomeScreenFilterRepository {

    override fun getFilters(): Flow<List<HomeScreenFilter>> {
        return wrapper.state
    }

    override suspend fun putFilters(filters: List<HomeScreenFilter>) {
        wrapper.transform { filters }
    }
}