/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.domain.interactors

import app.tivi.data.repositories.recommendedshows.RecommendedShowsRepository
import app.tivi.domain.Interactor
import app.tivi.inject.ProcessLifetime
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus

class UpdateRecommendedShows @Inject constructor(
    private val recommendedShowsRepository: RecommendedShowsRepository,
    dispatchers: AppCoroutineDispatchers,
    private val traktManager: TraktManager,
    @ProcessLifetime val processScope: CoroutineScope
) : Interactor<UpdateRecommendedShows.Params>() {
    override val scope: CoroutineScope = processScope + dispatchers.io

    override suspend fun doWork(params: Params) {
        if (traktManager.state.first() != TraktAuthState.LOGGED_IN) {
            // If we're not logged in, we can't load the recommended shows
            return
        }
        when (params.page) {
            Page.NEXT_PAGE -> {
                recommendedShowsRepository.loadNextPage()
            }
            Page.REFRESH -> {
                if (params.forceRefresh || recommendedShowsRepository.needUpdate()) {
                    recommendedShowsRepository.update()
                }
            }
        }
    }

    data class Params(val page: Page, val forceRefresh: Boolean)

    enum class Page {
        NEXT_PAGE, REFRESH
    }
}
