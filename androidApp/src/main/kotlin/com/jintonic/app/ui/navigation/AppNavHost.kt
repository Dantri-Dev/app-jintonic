package com.jintonic.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.jintonic.app.pvt.PvtMode
import com.jintonic.app.ui.body.BodyScreen
import com.jintonic.app.ui.emergency.EmergencyScreen
import com.jintonic.app.ui.health.HealthScreen
import com.jintonic.app.ui.health.HealthViewModel
import com.jintonic.app.ui.history.HistoryScreen
import com.jintonic.app.ui.home.HomeScreen
import com.jintonic.app.ui.onboarding.OnboardingScreen
import com.jintonic.app.ui.test_flow.PreTestScreen
import com.jintonic.app.ui.test_flow.PvtScreen
import com.jintonic.app.ui.test_flow.ResultScreen
import com.jintonic.app.ui.test_flow.TestFlowViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    heartRatePermission: String,
    hasPermission: Boolean,
    onPermissionResult: (Boolean) -> Unit,
) {
    val nav = rememberSwipeDismissableNavController()
    // Activity-scoped: shared by the screens of each flow.
    val testVm: TestFlowViewModel = koinViewModel()
    val healthVm: HealthViewModel = koinViewModel()

    fun startTest(mode: PvtMode, withPreTest: Boolean) {
        testVm.begin(mode)
        nav.navigate(if (withPreTest) Routes.PRE_TEST else Routes.PVT)
    }

    AppScaffold {
        SwipeDismissableNavHost(
            navController = nav,
            startDestination = if (hasPermission) Routes.HOME else Routes.ONBOARDING,
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    heartRatePermission = heartRatePermission,
                    onPermissionResult = onPermissionResult,
                    onContinue = {
                        nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onStartTest = { startTest(PvtMode.DEMO, withPreTest = true) },
                    onNavigate = { nav.navigate(it) },
                )
            }
            composable(Routes.PRE_TEST) { PreTestScreen(testVm, onConfirm = { nav.navigate(Routes.PVT) }) }
            composable(Routes.PVT) {
                PvtScreen(testVm, onFinished = {
                    nav.navigate(Routes.RESULT) { popUpTo(Routes.HOME) }
                })
            }
            composable(Routes.RESULT) {
                ResultScreen(
                    vm = testVm,
                    onDone = { nav.popBackStack(Routes.HOME, inclusive = false) },
                    onRetry = { startTest(testVm.mode, withPreTest = false) },
                    onHistory = { nav.navigate(Routes.HISTORY) },
                )
            }
            composable(Routes.HISTORY) { HistoryScreen() }
            composable(Routes.PARAMS) { HealthScreen(healthVm) }
            composable(Routes.BODY) { BodyScreen(healthVm) }
            composable(Routes.EMERGENCY) {
                EmergencyScreen(healthVm, onQuickTest = { startTest(PvtMode.TRIAGE, withPreTest = false) })
            }
        }
    }
}
