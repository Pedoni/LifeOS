package it.edon.lifeos.feature.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.account.DeleteAccountUseCase
import it.edon.lifeos.core.domain.account.UpdateDisplayNameUseCase
import it.edon.lifeos.core.domain.auth.AuthValidation
import it.edon.lifeos.core.domain.auth.DisplayNameIssue
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.UserDataRepository
import it.edon.lifeos.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val user = AuthUser("u1", "ada@example.com", "Ada")
    private val authState = MutableStateFlow<AuthState>(AuthState.SignedIn(user))
    private val preferences = MutableStateFlow(UserPreferences())
    private val authRepository = mockk<AuthRepository>()
    private val preferencesRepository = mockk<UserPreferencesRepository>(relaxed = true)
    private val userData = mockk<UserDataRepository>(relaxed = true)

    init {
        every { authRepository.authState } returns authState
        every { preferencesRepository.preferences } returns preferences
        coEvery { authRepository.updateDisplayName(any()) } returns Outcome.Success(Unit)
        coEvery { authRepository.deleteAccount() } returns Outcome.Success(Unit)
        coEvery { authRepository.signOut() } returns Outcome.Success(Unit)
    }

    private fun viewModel() = ProfileViewModel(
        authRepository,
        preferencesRepository,
        UpdateDisplayNameUseCase(authRepository),
        DeleteAccountUseCase(authRepository, userData)
    )

    // The state only runs while somebody collects it, as the screen does.
    private fun TestScope.started(): ProfileViewModel = viewModel().also {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { it.uiState.collect {} }
    }

    private fun ProfileViewModel.ready() = uiState.value as ProfileUiState.Ready

    @Test
    fun isLoadingUntilThereIsASignedInUser() = runTest {
        authState.value = AuthState.Loading
        val viewModel = started()
        assertEquals(ProfileUiState.Loading, viewModel.uiState.value)

        authState.value = AuthState.SignedOut
        assertEquals(ProfileUiState.Loading, viewModel.uiState.value)

        authState.value = AuthState.SignedIn(user)
        assertEquals("Ada", viewModel.ready().displayName)
    }

    @Test
    fun showsTheAccountAndThePreferences() = runTest {
        preferences.value = UserPreferences(themeMode = ThemeMode.DARK)
        val state = started().ready()

        assertEquals(user, state.user)
        assertEquals(ThemeMode.DARK, state.preferences.themeMode)
        assertFalse(state.canSaveDisplayName)
    }

    @Test
    fun theNameCanBeSavedOnlyOnceItHasChanged() = runTest {
        val viewModel = started()

        viewModel.onEvent(ProfileEvent.DisplayNameChanged("Ada Lovelace"))
        assertTrue(viewModel.ready().canSaveDisplayName)
        assertEquals("Ada Lovelace", viewModel.ready().displayName)

        viewModel.onEvent(ProfileEvent.DisplayNameChanged("  Ada "))
        assertFalse(viewModel.ready().canSaveDisplayName)
    }

    @Test
    fun savingSendsTheNameAndDropsTheDraft() = runTest {
        val viewModel = started()
        viewModel.onEvent(ProfileEvent.DisplayNameChanged(" Ada Lovelace "))

        viewModel.onEvent(ProfileEvent.SaveDisplayName)

        coVerify(exactly = 1) { authRepository.updateDisplayName("Ada Lovelace") }
        assertFalse(viewModel.ready().isBusy)
        // The stored name comes back through the account state.
        authState.value = AuthState.SignedIn(user.copy(displayName = "Ada Lovelace"))
        assertEquals("Ada Lovelace", viewModel.ready().displayName)
        assertFalse(viewModel.ready().canSaveDisplayName)
    }

    @Test
    fun aTooLongNameIsRejectedWithoutCallingTheServer() = runTest {
        val viewModel = started()
        viewModel.onEvent(
            ProfileEvent.DisplayNameChanged("a".repeat(AuthValidation.MAX_DISPLAY_NAME_LENGTH + 1))
        )

        viewModel.onEvent(ProfileEvent.SaveDisplayName)

        assertEquals(DisplayNameIssue.TOO_LONG, viewModel.ready().displayNameIssue)
        coVerify(exactly = 0) { authRepository.updateDisplayName(any()) }
    }

    @Test
    fun aFailedSaveKeepsTheDraftAndShowsTheError() = runTest {
        coEvery { authRepository.updateDisplayName(any()) } returns
            Outcome.Failure(AuthError.Network)
        val viewModel = started()
        viewModel.onEvent(ProfileEvent.DisplayNameChanged("Grace"))

        viewModel.onEvent(ProfileEvent.SaveDisplayName)

        val state = viewModel.ready()
        assertEquals(AuthError.Network, state.error)
        assertEquals("Grace", state.displayName)
        assertTrue(state.canSaveDisplayName)
        assertFalse(state.isBusy)
    }

    @Test
    fun editingTheNameClearsTheError() = runTest {
        coEvery { authRepository.updateDisplayName(any()) } returns
            Outcome.Failure(AuthError.Network)
        val viewModel = started()
        viewModel.onEvent(ProfileEvent.DisplayNameChanged("Grace"))
        viewModel.onEvent(ProfileEvent.SaveDisplayName)

        viewModel.onEvent(ProfileEvent.DisplayNameChanged("Grace H"))

        assertNull(viewModel.ready().error)
    }

    @Test
    fun preferencesAreForwardedToTheRepository() = runTest {
        val viewModel = started()

        viewModel.onEvent(ProfileEvent.ThemeModeSelected(ThemeMode.LIGHT))
        viewModel.onEvent(ProfileEvent.DynamicColorChanged(true))

        coVerify { preferencesRepository.setThemeMode(ThemeMode.LIGHT) }
        coVerify { preferencesRepository.setUseDynamicColor(true) }
    }

    @Test
    fun signingOutAsksTheRepository() = runTest {
        val viewModel = started()

        viewModel.onEvent(ProfileEvent.SignOut)

        coVerify(exactly = 1) { authRepository.signOut() }
        assertNull(viewModel.ready().error)
    }

    @Test
    fun aFailedSignOutIsReported() = runTest {
        coEvery { authRepository.signOut() } returns Outcome.Failure(AuthError.Unknown)
        val viewModel = started()

        viewModel.onEvent(ProfileEvent.SignOut)

        assertEquals(AuthError.Unknown, viewModel.ready().error)
        assertFalse(viewModel.ready().isBusy)
    }

    @Test
    fun deletingTheAccountNeedsConfirmation() = runTest {
        val viewModel = started()

        viewModel.onEvent(ProfileEvent.RequestAccountDeletion)
        assertTrue(viewModel.ready().isDeleteDialogVisible)
        coVerify(exactly = 0) { authRepository.deleteAccount() }

        viewModel.onEvent(ProfileEvent.DismissAccountDeletion)
        assertFalse(viewModel.ready().isDeleteDialogVisible)
        coVerify(exactly = 0) { authRepository.deleteAccount() }
    }

    @Test
    fun confirmingDeletesTheServerAccountThenTheLocalDataThenSignsOut() = runTest {
        val viewModel = started()
        viewModel.onEvent(ProfileEvent.RequestAccountDeletion)

        viewModel.onEvent(ProfileEvent.ConfirmAccountDeletion)

        coVerifyOrder {
            authRepository.deleteAccount()
            userData.deleteAllFor("u1")
            authRepository.signOut()
        }
        assertFalse(viewModel.ready().isDeleteDialogVisible)
    }

    @Test
    fun aFailedDeletionKeepsTheLocalDataAndShowsTheError() = runTest {
        coEvery { authRepository.deleteAccount() } returns Outcome.Failure(AuthError.Network)
        val viewModel = started()
        viewModel.onEvent(ProfileEvent.RequestAccountDeletion)

        viewModel.onEvent(ProfileEvent.ConfirmAccountDeletion)

        coVerify(exactly = 0) { userData.deleteAllFor(any()) }
        coVerify(exactly = 0) { authRepository.signOut() }
        assertEquals(AuthError.Network, viewModel.ready().error)
        assertFalse(viewModel.ready().isDeleteDialogVisible)
        assertFalse(viewModel.ready().isBusy)
    }
}
