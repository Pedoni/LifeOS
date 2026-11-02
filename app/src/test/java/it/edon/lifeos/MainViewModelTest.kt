package it.edon.lifeos

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences
import it.edon.lifeos.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startsLoadingUntilThePreferencesAreRead() {
        val repository = mockk<UserPreferencesRepository>()
        every { repository.preferences } returns MutableSharedFlow()

        val viewModel = MainViewModel(repository)

        assertEquals(MainUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun exposesTheStoredPreferences() = runTest {
        val stored = UserPreferences(ThemeMode.DARK, useDynamicColor = true)
        val repository = mockk<UserPreferencesRepository>()
        every { repository.preferences } returns flowOf(stored)

        MainViewModel(repository).uiState.test {
            var state = awaitItem()
            if (state is MainUiState.Loading) state = awaitItem()
            assertEquals(MainUiState.Success(stored), state)
        }
    }
}
