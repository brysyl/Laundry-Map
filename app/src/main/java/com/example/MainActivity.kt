package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.di.AppContainer
import com.example.domain.model.NotificationType
import com.example.domain.model.Order
import com.example.domain.model.UserRole
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.PersonaHeader
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.courier.CourierAppScreen
import com.example.ui.screens.customer.CustomerHomeScreen
import com.example.ui.screens.customer.OrderTrackingScreen
import com.example.ui.screens.customer.PaystackCheckoutSheet
import com.example.ui.screens.vendor.VendorPortalScreen
import com.example.ui.theme.LaundryMapTheme
import com.example.ui.viewmodel.AppPersona
import com.example.ui.viewmodel.AppPersonaViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CourierViewModel
import com.example.ui.viewmodel.CustomerViewModel
import com.example.ui.viewmodel.NotificationViewModel
import com.example.ui.viewmodel.VendorViewModel

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.Factory(
            authRepository = appContainer.authRepository,
            sessionManager = appContainer.sessionManager
        )
    }

    private val notificationViewModel: NotificationViewModel by viewModels {
        NotificationViewModel.Factory(
            notificationRepository = appContainer.notificationRepository,
            pushService = appContainer.pushService
        )
    }

    private val personaViewModel: AppPersonaViewModel by viewModels {
        AppPersonaViewModel.Factory(appContainer.offlineSyncManager)
    }

    private val customerViewModel: CustomerViewModel by viewModels {
        CustomerViewModel.Factory(
            orderRepository = appContainer.orderRepository,
            laundromatRepository = appContainer.laundromatRepository,
            webSocketClient = appContainer.webSocketCourierClient
        )
    }

    private val vendorViewModel: VendorViewModel by viewModels {
        VendorViewModel.Factory(
            orderRepository = appContainer.orderRepository,
            laundromatRepository = appContainer.laundromatRepository
        )
    }

    private val courierViewModel: CourierViewModel by viewModels {
        CourierViewModel.Factory(
            orderRepository = appContainer.orderRepository,
            webSocketClient = appContainer.webSocketCourierClient
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)
        enableEdgeToEdge()

        setContent {
            LaundryMapTheme {
                val currentSession by authViewModel.currentSession.collectAsStateWithLifecycle()
                val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

                val personaState by personaViewModel.uiState.collectAsStateWithLifecycle()
                val customerState by customerViewModel.uiState.collectAsStateWithLifecycle()
                val activeOrders by customerViewModel.activeOrders.collectAsStateWithLifecycle()
                val vendorState by vendorViewModel.uiState.collectAsStateWithLifecycle()
                val vendorOrders by vendorViewModel.laundromatOrders.collectAsStateWithLifecycle()
                val courierState by courierViewModel.uiState.collectAsStateWithLifecycle()
                val courierOrders by courierViewModel.assignedOrders.collectAsStateWithLifecycle()

                val inAppBanner by notificationViewModel.inAppBannerAlert.collectAsStateWithLifecycle()
                val notificationsList by notificationViewModel.notifications.collectAsStateWithLifecycle()
                val unreadNotifsCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()
                val isNotificationCenterOpen by notificationViewModel.isCenterOpen.collectAsStateWithLifecycle()

                // Request POST_NOTIFICATIONS permission on Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Result handled */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Sync notification viewmodel role with active persona
                LaunchedEffect(personaState.activePersona) {
                    val matchingRole = when (personaState.activePersona) {
                        AppPersona.CUSTOMER -> UserRole.CUSTOMER
                        AppPersona.VENDOR -> UserRole.VENDOR
                        AppPersona.COURIER -> UserRole.COURIER
                    }
                    notificationViewModel.setActiveRole(matchingRole)
                }

                // Sync persona with session on initial login
                LaunchedEffect(currentSession?.role) {
                    currentSession?.role?.let { role ->
                        val matchingPersona = when (role) {
                            UserRole.CUSTOMER -> AppPersona.CUSTOMER
                            UserRole.VENDOR -> AppPersona.VENDOR
                            UserRole.COURIER -> AppPersona.COURIER
                        }
                        personaViewModel.switchPersona(matchingPersona)
                    }
                }

                if (currentSession == null) {
                    // Render Auth Flow (Login, Register, Forgot Password)
                    AuthScreen(
                        state = authUiState,
                        onTabSelected = { authViewModel.selectTab(it) },
                        onRoleSelected = { authViewModel.selectRole(it) },
                        onEmailChanged = { authViewModel.updateEmail(it) },
                        onPasswordChanged = { authViewModel.updatePassword(it) },
                        onFullNameChanged = { authViewModel.updateFullName(it) },
                        onPhoneChanged = { authViewModel.updatePhone(it) },
                        onBusinessNameChanged = { authViewModel.updateBusinessName(it) },
                        onVehiclePlateChanged = { authViewModel.updateVehiclePlate(it) },
                        onResetEmailChanged = { authViewModel.updateResetEmail(it) },
                        onResetOtpChanged = { authViewModel.updateResetOtp(it) },
                        onResetNewPasswordChanged = { authViewModel.updateResetNewPassword(it) },
                        onQuickFillDemo = { authViewModel.quickFillDemo(it) },
                        onLoginClicked = { authViewModel.login() },
                        onRegisterClicked = { authViewModel.register() },
                        onRequestResetClicked = { authViewModel.requestPasswordReset() },
                        onConfirmResetClicked = { authViewModel.confirmPasswordReset() },
                        modifier = Modifier.statusBarsPadding()
                    )
                } else {
                    // Check if customer is actively tracking a specific order
                    val trackedOrder = customerState.activeTrackedOrderId?.let { id ->
                        activeOrders.find { it.id == id }
                    }

                    BackHandler(enabled = customerState.activeTrackedOrderId != null) {
                        customerViewModel.selectOrderForTracking(null)
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            Column(modifier = Modifier.statusBarsPadding()) {
                                PersonaHeader(
                                    state = personaState,
                                    unreadNotificationsCount = unreadNotifsCount,
                                    userName = currentSession?.fullName,
                                    onPersonaSelected = { personaViewModel.switchPersona(it) },
                                    onCorridorToggled = { personaViewModel.switchCorridor(it) },
                                    onNetworkToggled = { personaViewModel.toggleNetworkOnline() },
                                    onOpenNotifications = { notificationViewModel.openNotificationCenter() },
                                    onLogout = { authViewModel.logout() }
                                )

                                // Real-Time In-App Push Alert Banner
                                InAppNotificationBanner(
                                    notification = inAppBanner,
                                    onDismiss = { notificationViewModel.dismissBanner() },
                                    onNotificationClicked = { notif ->
                                        notificationViewModel.dismissBanner()
                                        notif.orderId?.let { id ->
                                            customerViewModel.selectOrderForTracking(id)
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (trackedOrder != null && personaState.activePersona == AppPersona.CUSTOMER) {
                                OrderTrackingScreen(
                                    order = trackedOrder,
                                    telemetry = customerState.latestCourierTelemetry,
                                    onBackClicked = { customerViewModel.selectOrderForTracking(null) }
                                )
                            } else {
                                when (personaState.activePersona) {
                                    AppPersona.CUSTOMER -> {
                                        CustomerHomeScreen(
                                            state = customerState,
                                            activeOrders = activeOrders,
                                            onSelectService = { customerViewModel.selectService(it) },
                                            onSelectLocation = { customerViewModel.selectLocation(it) },
                                            onUpdateAddressDetail = { customerViewModel.updateAddressDetails(it) },
                                            onUpdateItemQuantity = { id, delta -> customerViewModel.updateItemQuantity(id, delta) },
                                            onCheckoutClicked = { customerViewModel.openCheckoutSheet() },
                                            onTrackOrderClicked = { customerViewModel.selectOrderForTracking(it) }
                                        )

                                        if (customerState.isCheckoutSheetOpen) {
                                            PaystackCheckoutSheet(
                                                totalNgn = customerState.totalNgn,
                                                subtotalNgn = customerState.subtotalNgn,
                                                deliveryFeeNgn = customerState.deliveryFeeNgn,
                                                isProcessing = customerState.isPaymentProcessing,
                                                onPayConfirmed = { provider ->
                                                    customerViewModel.processPaystackCheckout(provider)
                                                },
                                                onDismiss = { customerViewModel.closeCheckoutSheet() }
                                            )
                                        }
                                    }

                                    AppPersona.VENDOR -> {
                                        VendorPortalScreen(
                                            state = vendorState,
                                            orders = vendorOrders,
                                            onAcceptOrder = { vendorViewModel.acceptIncomingOrder(it) },
                                            onMarkOrderAtLaundromat = { vendorViewModel.markOrderAtLaundromat(it) },
                                            onAdvanceProcessing = { id, stage -> vendorViewModel.advanceProcessing(id, stage) },
                                            onReadyForDispatch = { vendorViewModel.readyForCourierDispatch(it) }
                                        )
                                    }

                                    AppPersona.COURIER -> {
                                        CourierAppScreen(
                                            state = courierState,
                                            assignedOrders = courierOrders,
                                            onToggleAvailability = { courierViewModel.toggleAvailability() },
                                            onToggleNavigation = { courierViewModel.toggleNavigationHud() },
                                            onPickUpFromCustomer = { courierViewModel.pickUpFromCustomer(it) },
                                            onDeliverToLaundromat = { courierViewModel.deliverToLaundromat(it) },
                                            onOpenDeliveryModal = { courierViewModel.openDeliveryVerification(it) },
                                            onCloseDeliveryModal = { courierViewModel.closeDeliveryVerification() },
                                            onOtpEntered = { courierViewModel.setEnteredOtp(it) },
                                            onConfirmDelivery = { signaturePath ->
                                                courierViewModel.confirmDeliveryWithSignature(signaturePath)
                                            }
                                        )
                                    }
                                }
                            }

                            // Notification Center Bottom Sheet
                            if (isNotificationCenterOpen) {
                                NotificationCenterSheet(
                                    activeRole = when (personaState.activePersona) {
                                        AppPersona.CUSTOMER -> UserRole.CUSTOMER
                                        AppPersona.VENDOR -> UserRole.VENDOR
                                        AppPersona.COURIER -> UserRole.COURIER
                                    },
                                    notifications = notificationsList,
                                    onMarkAsRead = { notificationViewModel.markAsRead(it) },
                                    onMarkAllAsRead = { notificationViewModel.markAllAsRead() },
                                    onTriggerTestPush = {
                                        notificationViewModel.triggerSimulatedPush(NotificationType.ORDER_STATUS_UPDATE)
                                    },
                                    onDismiss = { notificationViewModel.closeNotificationCenter() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
