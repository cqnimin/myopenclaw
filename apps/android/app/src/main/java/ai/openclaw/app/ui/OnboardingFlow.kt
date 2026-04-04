package ai.openclaw.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ai.openclaw.app.BuildConfig
import ai.openclaw.app.R
import ai.openclaw.app.LocationMode
import ai.openclaw.app.MainViewModel
import ai.openclaw.app.gateway.GatewayEndpoint
import ai.openclaw.app.node.DeviceNotificationListenerService
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

private enum class OnboardingStep(val index: Int) {
  Welcome(1),
  Gateway(2),
  Permissions(3),
  FinalCheck(4),
}

@Composable
private fun OnboardingStep.localizedLabel(): String = when (this) {
  OnboardingStep.Welcome -> stringResource(R.string.onboarding_step_welcome)
  OnboardingStep.Gateway -> stringResource(R.string.onboarding_step_gateway)
  OnboardingStep.Permissions -> stringResource(R.string.onboarding_step_permissions)
  OnboardingStep.FinalCheck -> stringResource(R.string.onboarding_step_connect)
}

private enum class GatewayInputMode {
  SetupCode,
  Manual,
}

private enum class PermissionToggle {
  Discovery,
  Location,
  Notifications,
  Microphone,
  Camera,
  Photos,
  Contacts,
  Calendar,
  Motion,
  Sms,
  CallLog,
}

private enum class SpecialAccessToggle {
  NotificationListener,
}

private val onboardingBackgroundGradient: Brush
  @Composable get() = mobileBackgroundGradient

private val onboardingSurface: Color
  @Composable get() = mobileCardSurface

private val onboardingBorder: Color
  @Composable get() = mobileBorder

private val onboardingBorderStrong: Color
  @Composable get() = mobileBorderStrong

private val onboardingText: Color
  @Composable get() = mobileText

private val onboardingTextSecondary: Color
  @Composable get() = mobileTextSecondary

private val onboardingTextTertiary: Color
  @Composable get() = mobileTextTertiary

private val onboardingAccent: Color
  @Composable get() = mobileAccent

private val onboardingAccentSoft: Color
  @Composable get() = mobileAccentSoft

private val onboardingAccentBorderStrong: Color
  @Composable get() = mobileAccentBorderStrong

private val onboardingSuccess: Color
  @Composable get() = mobileSuccess

private val onboardingSuccessSoft: Color
  @Composable get() = mobileSuccessSoft

private val onboardingWarning: Color
  @Composable get() = mobileWarning

private val onboardingWarningSoft: Color
  @Composable get() = mobileWarningSoft

private val onboardingCommandBg: Color
  @Composable get() = mobileCodeBg

private val onboardingCommandBorder: Color
  @Composable get() = mobileCodeBorder

private val onboardingCommandAccent: Color
  @Composable get() = mobileCodeAccent

private val onboardingCommandText: Color
  @Composable get() = mobileCodeText

private val onboardingDisplayStyle: TextStyle
  get() = mobileDisplay

private val onboardingTitle1Style: TextStyle
  get() = mobileTitle1

private val onboardingHeadlineStyle: TextStyle
  get() = mobileHeadline

private val onboardingBodyStyle: TextStyle
  get() = mobileBody

private val onboardingCalloutStyle: TextStyle
  get() = mobileCallout

private val onboardingCaption1Style: TextStyle
  get() = mobileCaption1

private val onboardingCaption2Style: TextStyle
  get() = mobileCaption2

@Composable
fun OnboardingFlow(viewModel: MainViewModel, modifier: Modifier = Modifier) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val statusText by viewModel.statusText.collectAsState()
  val isConnected by viewModel.isConnected.collectAsState()
  val isNodeConnected by viewModel.isNodeConnected.collectAsState()
  val serverName by viewModel.serverName.collectAsState()
  val remoteAddress by viewModel.remoteAddress.collectAsState()
  val persistedGatewayToken by viewModel.gatewayToken.collectAsState()
  val pendingTrust by viewModel.pendingGatewayTrust.collectAsState()

  var step by rememberSaveable { mutableStateOf(OnboardingStep.Welcome) }
  var setupCode by rememberSaveable { mutableStateOf("") }
  var gatewayUrl by rememberSaveable { mutableStateOf("") }
  var gatewayPassword by rememberSaveable { mutableStateOf("") }
  var gatewayInputMode by rememberSaveable { mutableStateOf(GatewayInputMode.SetupCode) }
  var gatewayAdvancedOpen by rememberSaveable { mutableStateOf(false) }
  var manualHost by rememberSaveable { mutableStateOf("10.0.2.2") }
  var manualPort by rememberSaveable { mutableStateOf("18789") }
  var manualTls by rememberSaveable { mutableStateOf(false) }
  var gatewayError by rememberSaveable { mutableStateOf<String?>(null) }
  var attemptedConnect by rememberSaveable { mutableStateOf(false) }
  val canFinishOnboarding = canFinishOnboarding(isConnected = isConnected, isNodeConnected = isNodeConnected)

  val lifecycleOwner = LocalLifecycleOwner.current
  val qrScannerOptions =
    remember {
      GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    }
  val qrScanner = remember(context, qrScannerOptions) { GmsBarcodeScanning.getClient(context, qrScannerOptions) }

  val smsAvailable =
    remember(context) {
      BuildConfig.OPENCLAW_ENABLE_SMS &&
        context.packageManager?.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) == true
    }
  val callLogAvailable = remember { BuildConfig.OPENCLAW_ENABLE_CALL_LOG }
  val motionAvailable =
    remember(context) {
      hasMotionCapabilities(context)
    }
  val motionPermissionRequired = true
  val notificationsPermissionRequired = Build.VERSION.SDK_INT >= 33
  val discoveryPermission =
    if (Build.VERSION.SDK_INT >= 33) {
      Manifest.permission.NEARBY_WIFI_DEVICES
    } else {
      Manifest.permission.ACCESS_FINE_LOCATION
    }
  val photosPermission =
    if (Build.VERSION.SDK_INT >= 33) {
      Manifest.permission.READ_MEDIA_IMAGES
    } else {
      Manifest.permission.READ_EXTERNAL_STORAGE
    }

  var enableDiscovery by
    rememberSaveable {
      mutableStateOf(isPermissionGranted(context, discoveryPermission))
    }
  var enableLocation by rememberSaveable { mutableStateOf(false) }
  var enableNotifications by
    rememberSaveable {
      mutableStateOf(
        !notificationsPermissionRequired ||
          isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS),
      )
    }
  var enableNotificationListener by
    rememberSaveable {
      mutableStateOf(isNotificationListenerEnabled(context))
    }
  var enableMicrophone by rememberSaveable { mutableStateOf(false) }
  var enableCamera by rememberSaveable { mutableStateOf(false) }
  var enablePhotos by rememberSaveable { mutableStateOf(false) }
  var enableContacts by rememberSaveable { mutableStateOf(false) }
  var enableCalendar by rememberSaveable { mutableStateOf(false) }
  var enableMotion by
    rememberSaveable {
      mutableStateOf(
        motionAvailable &&
          (!motionPermissionRequired || isPermissionGranted(context, Manifest.permission.ACTIVITY_RECOGNITION)),
      )
    }
  var enableSms by
    rememberSaveable {
      mutableStateOf(
        smsAvailable &&
                isPermissionGranted(context, Manifest.permission.SEND_SMS) &&
                isPermissionGranted(context, Manifest.permission.READ_SMS)
      )
    }
  var enableCallLog by
    rememberSaveable {
      mutableStateOf(callLogAvailable && isPermissionGranted(context, Manifest.permission.READ_CALL_LOG))
    }

  var pendingPermissionToggle by remember { mutableStateOf<PermissionToggle?>(null) }
  var pendingSpecialAccessToggle by remember { mutableStateOf<SpecialAccessToggle?>(null) }

  fun setPermissionToggleEnabled(toggle: PermissionToggle, enabled: Boolean) {
    when (toggle) {
      PermissionToggle.Discovery -> enableDiscovery = enabled
      PermissionToggle.Location -> enableLocation = enabled
      PermissionToggle.Notifications -> enableNotifications = enabled
      PermissionToggle.Microphone -> enableMicrophone = enabled
      PermissionToggle.Camera -> enableCamera = enabled
      PermissionToggle.Photos -> enablePhotos = enabled
      PermissionToggle.Contacts -> enableContacts = enabled
      PermissionToggle.Calendar -> enableCalendar = enabled
      PermissionToggle.Motion -> enableMotion = enabled && motionAvailable
      PermissionToggle.Sms -> enableSms = enabled && smsAvailable
      PermissionToggle.CallLog -> enableCallLog = enabled && callLogAvailable
    }
  }

  fun isPermissionToggleGranted(toggle: PermissionToggle): Boolean =
    when (toggle) {
      PermissionToggle.Discovery -> isPermissionGranted(context, discoveryPermission)
      PermissionToggle.Location ->
        isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
          isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
      PermissionToggle.Notifications ->
        !notificationsPermissionRequired ||
          isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
      PermissionToggle.Microphone -> isPermissionGranted(context, Manifest.permission.RECORD_AUDIO)
      PermissionToggle.Camera -> isPermissionGranted(context, Manifest.permission.CAMERA)
      PermissionToggle.Photos -> isPermissionGranted(context, photosPermission)
      PermissionToggle.Contacts ->
        isPermissionGranted(context, Manifest.permission.READ_CONTACTS) &&
          isPermissionGranted(context, Manifest.permission.WRITE_CONTACTS)
      PermissionToggle.Calendar ->
        isPermissionGranted(context, Manifest.permission.READ_CALENDAR) &&
          isPermissionGranted(context, Manifest.permission.WRITE_CALENDAR)
      PermissionToggle.Motion ->
        !motionAvailable ||
          !motionPermissionRequired ||
          isPermissionGranted(context, Manifest.permission.ACTIVITY_RECOGNITION)
      PermissionToggle.Sms ->
        !smsAvailable ||
                (isPermissionGranted(context, Manifest.permission.SEND_SMS) &&
                        isPermissionGranted(context, Manifest.permission.READ_SMS))
      PermissionToggle.CallLog ->
        !callLogAvailable || isPermissionGranted(context, Manifest.permission.READ_CALL_LOG)
    }

  fun setSpecialAccessToggleEnabled(toggle: SpecialAccessToggle, enabled: Boolean) {
    when (toggle) {
      SpecialAccessToggle.NotificationListener -> enableNotificationListener = enabled
    }
  }

  val sumDiscovery = stringResource(R.string.perm_sum_gateway_discovery)
  val sumLocation = stringResource(R.string.perm_sum_location)
  val sumNotifications = stringResource(R.string.perm_sum_notifications)
  val sumNotifListener = stringResource(R.string.perm_sum_notif_listener)
  val sumMicrophone = stringResource(R.string.perm_sum_microphone)
  val sumCamera = stringResource(R.string.perm_sum_camera)
  val sumPhotos = stringResource(R.string.perm_sum_photos)
  val sumContacts = stringResource(R.string.perm_sum_contacts)
  val sumCalendar = stringResource(R.string.perm_sum_calendar)
  val sumMotion = stringResource(R.string.perm_sum_motion)
  val sumSms = stringResource(R.string.perm_sum_sms)
  val sumCallLog = stringResource(R.string.perm_sum_call_log)
  val sumNone = stringResource(R.string.perm_sum_none)

  val errQrInvalid = stringResource(R.string.onboarding_error_qr_invalid)
  val errQrScanner = stringResource(R.string.onboarding_error_qr_scanner)
  val errScanFirst = stringResource(R.string.onboarding_error_scan_first)
  val errInvalidSetupUrl = stringResource(R.string.onboarding_error_invalid_setup_url)
  val errManualInvalid = stringResource(R.string.onboarding_error_manual_invalid)
  val errInvalidGateway = stringResource(R.string.onboarding_error_invalid_gateway)
  val methodQr = stringResource(R.string.onboarding_method_qr)
  val methodManual = stringResource(R.string.onboarding_method_manual)

  val enabledPermissionSummary =
    remember(
      enableDiscovery,
      enableLocation,
      enableNotifications,
      enableNotificationListener,
      enableMicrophone,
      enableCamera,
      enablePhotos,
      enableContacts,
      enableCalendar,
      enableMotion,
      enableSms,
      enableCallLog,
      smsAvailable,
      callLogAvailable,
      motionAvailable,
    ) {
      val enabled = mutableListOf<String>()
      if (enableDiscovery) enabled += sumDiscovery
      if (enableLocation) enabled += sumLocation
      if (enableNotifications) enabled += sumNotifications
      if (enableNotificationListener) enabled += sumNotifListener
      if (enableMicrophone) enabled += sumMicrophone
      if (enableCamera) enabled += sumCamera
      if (enablePhotos) enabled += sumPhotos
      if (enableContacts) enabled += sumContacts
      if (enableCalendar) enabled += sumCalendar
      if (enableMotion && motionAvailable) enabled += sumMotion
      if (smsAvailable && enableSms) enabled += sumSms
      if (callLogAvailable && enableCallLog) enabled += sumCallLog
      if (enabled.isEmpty()) sumNone else enabled.joinToString(", ")
    }

  val proceedFromPermissions: () -> Unit = proceed@{
    var openedSpecialSetup = false
    if (enableNotificationListener && !isNotificationListenerEnabled(context)) {
      openNotificationListenerSettings(context)
      openedSpecialSetup = true
    }
    if (openedSpecialSetup) {
      return@proceed
    }
    step = OnboardingStep.FinalCheck
  }

  val togglePermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
      val pendingToggle = pendingPermissionToggle ?: return@rememberLauncherForActivityResult
      setPermissionToggleEnabled(pendingToggle, isPermissionToggleGranted(pendingToggle))
      pendingPermissionToggle = null
    }

  val requestPermissionToggle: (PermissionToggle, Boolean, List<String>) -> Unit =
    request@{ toggle, enabled, permissions ->
      if (!enabled) {
        setPermissionToggleEnabled(toggle, false)
        return@request
      }
      if (isPermissionToggleGranted(toggle)) {
        setPermissionToggleEnabled(toggle, true)
        return@request
      }
      val missing = permissions.distinct().filterNot { isPermissionGranted(context, it) }
      if (missing.isEmpty()) {
        setPermissionToggleEnabled(toggle, isPermissionToggleGranted(toggle))
        return@request
      }
      pendingPermissionToggle = toggle
      togglePermissionLauncher.launch(missing.toTypedArray())
    }

  val requestSpecialAccessToggle: (SpecialAccessToggle, Boolean) -> Unit =
    request@{ toggle, enabled ->
      if (!enabled) {
        setSpecialAccessToggleEnabled(toggle, false)
        pendingSpecialAccessToggle = null
        return@request
      }
      val grantedNow =
        when (toggle) {
          SpecialAccessToggle.NotificationListener -> isNotificationListenerEnabled(context)
        }
      if (grantedNow) {
        setSpecialAccessToggleEnabled(toggle, true)
        pendingSpecialAccessToggle = null
        return@request
      }
      pendingSpecialAccessToggle = toggle
      when (toggle) {
        SpecialAccessToggle.NotificationListener -> openNotificationListenerSettings(context)
      }
    }

  DisposableEffect(lifecycleOwner, context, pendingSpecialAccessToggle) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event != Lifecycle.Event.ON_RESUME) {
          return@LifecycleEventObserver
        }
        when (pendingSpecialAccessToggle) {
          SpecialAccessToggle.NotificationListener -> {
            setSpecialAccessToggleEnabled(
              SpecialAccessToggle.NotificationListener,
              isNotificationListenerEnabled(context),
            )
            pendingSpecialAccessToggle = null
          }
          null -> Unit
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  if (pendingTrust != null) {
    val prompt = pendingTrust!!
    AlertDialog(
      onDismissRequest = { viewModel.declineGatewayTrustPrompt() },
      containerColor = onboardingSurface,
      title = { Text(stringResource(R.string.trust_gateway_title), style = onboardingHeadlineStyle, color = onboardingText) },
      text = {
        Text(
          stringResource(R.string.trust_gateway_body, prompt.fingerprintSha256),
          style = onboardingCalloutStyle,
          color = onboardingText,
        )
      },
      confirmButton = {
        TextButton(
          onClick = { viewModel.acceptGatewayTrustPrompt() },
          colors = ButtonDefaults.textButtonColors(contentColor = onboardingAccent),
        ) {
          Text(stringResource(R.string.trust_and_continue))
        }
      },
      dismissButton = {
        TextButton(
          onClick = { viewModel.declineGatewayTrustPrompt() },
          colors = ButtonDefaults.textButtonColors(contentColor = onboardingTextSecondary),
        ) {
          Text(stringResource(R.string.cancel))
        }
      },
    )
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(onboardingBackgroundGradient),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .imePadding()
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        Column(
          modifier = Modifier.padding(top = 12.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            "OpenClaw",
            style = onboardingDisplayStyle,
            color = onboardingText,
          )
          Text(
            stringResource(R.string.onboarding_subtitle),
            style = onboardingTitle1Style,
            color = onboardingTextSecondary,
          )
        }
        StepRail(current = step)

        when (step) {
          OnboardingStep.Welcome -> WelcomeStep()
          OnboardingStep.Gateway ->
            GatewayStep(
              inputMode = gatewayInputMode,
              advancedOpen = gatewayAdvancedOpen,
              setupCode = setupCode,
              manualHost = manualHost,
              manualPort = manualPort,
              manualTls = manualTls,
              gatewayToken = persistedGatewayToken,
              gatewayPassword = gatewayPassword,
              gatewayError = gatewayError,
              onScanQrClick = {
                gatewayError = null
                qrScanner.startScan()
                  .addOnSuccessListener { barcode ->
                    val contents = barcode.rawValue?.trim().orEmpty()
                    if (contents.isEmpty()) {
                      return@addOnSuccessListener
                    }
                    val scannedSetupCode = resolveScannedSetupCode(contents)
                    if (scannedSetupCode == null) {
                      gatewayError = errQrInvalid
                      return@addOnSuccessListener
                    }
                    setupCode = scannedSetupCode
                    gatewayInputMode = GatewayInputMode.SetupCode
                    gatewayError = null
                    attemptedConnect = false
                  }
                  .addOnCanceledListener {
                    // User dismissed the scanner; preserve current form state.
                  }
                  .addOnFailureListener {
                    gatewayError = errQrScanner
                  }
              },
              onAdvancedOpenChange = { gatewayAdvancedOpen = it },
              onInputModeChange = {
                gatewayInputMode = it
                gatewayError = null
              },
              onSetupCodeChange = {
                setupCode = it
                gatewayError = null
              },
              onManualHostChange = {
                manualHost = it
                gatewayError = null
              },
              onManualPortChange = {
                manualPort = it
                gatewayError = null
              },
              onManualTlsChange = { manualTls = it },
              onTokenChange = viewModel::setGatewayToken,
              onPasswordChange = { gatewayPassword = it },
            )
          OnboardingStep.Permissions ->
            PermissionsStep(
              enableDiscovery = enableDiscovery,
              enableLocation = enableLocation,
              enableNotifications = enableNotifications,
              enableNotificationListener = enableNotificationListener,
              enableMicrophone = enableMicrophone,
              enableCamera = enableCamera,
              enablePhotos = enablePhotos,
              enableContacts = enableContacts,
              enableCalendar = enableCalendar,
              enableMotion = enableMotion,
              motionAvailable = motionAvailable,
              motionPermissionRequired = motionPermissionRequired,
              enableSms = enableSms,
              smsAvailable = smsAvailable,
              callLogAvailable = callLogAvailable,
              enableCallLog = enableCallLog,
              context = context,
              onDiscoveryChange = { checked ->
                requestPermissionToggle(
                  PermissionToggle.Discovery,
                  checked,
                  listOf(discoveryPermission),
                )
              },
              onLocationChange = { checked ->
                requestPermissionToggle(
                  PermissionToggle.Location,
                  checked,
                  listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                  ),
                )
              },
              onNotificationsChange = { checked ->
                if (!notificationsPermissionRequired) {
                  setPermissionToggleEnabled(PermissionToggle.Notifications, checked)
                } else {
                  requestPermissionToggle(
                    PermissionToggle.Notifications,
                    checked,
                    listOf(Manifest.permission.POST_NOTIFICATIONS),
                  )
                }
              },
              onNotificationListenerChange = { checked ->
                requestSpecialAccessToggle(SpecialAccessToggle.NotificationListener, checked)
              },
              onMicrophoneChange = { checked ->
                requestPermissionToggle(
                  PermissionToggle.Microphone,
                  checked,
                  listOf(Manifest.permission.RECORD_AUDIO),
                )
              },
              onCameraChange = { checked ->
                requestPermissionToggle(
                  PermissionToggle.Camera,
                  checked,
                  listOf(Manifest.permission.CAMERA),
                )
              },
              onPhotosChange = { checked ->
                requestPermissionToggle(
                  PermissionToggle.Photos,
                  checked,
                  listOf(photosPermission),
                )
              },
              onContactsChange = { checked ->
                requestPermissionToggle(
                  PermissionToggle.Contacts,
                  checked,
                  listOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                  ),
                )
              },
              onCalendarChange = { checked ->
                requestPermissionToggle(
                  PermissionToggle.Calendar,
                  checked,
                  listOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR,
                  ),
                )
              },
              onMotionChange = { checked ->
                if (!motionAvailable) {
                  setPermissionToggleEnabled(PermissionToggle.Motion, false)
                } else if (!motionPermissionRequired) {
                  setPermissionToggleEnabled(PermissionToggle.Motion, checked)
                } else {
                  requestPermissionToggle(
                    PermissionToggle.Motion,
                    checked,
                    listOf(Manifest.permission.ACTIVITY_RECOGNITION),
                  )
                }
              },
              onSmsChange = { checked ->
                if (!smsAvailable) {
                  setPermissionToggleEnabled(PermissionToggle.Sms, false)
                } else {
                  requestPermissionToggle(
                    PermissionToggle.Sms,
                    checked,
                    listOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS),
                  )
                }
              },
              onCallLogChange = { checked ->
                if (!callLogAvailable) {
                  setPermissionToggleEnabled(PermissionToggle.CallLog, false)
                } else {
                  requestPermissionToggle(
                    PermissionToggle.CallLog,
                    checked,
                    listOf(Manifest.permission.READ_CALL_LOG),
                  )
                }
              },
            )
          OnboardingStep.FinalCheck ->
            FinalStep(
              parsedGateway = parseGatewayEndpoint(gatewayUrl),
              statusText = statusText,
              isConnected = canFinishOnboarding,
              serverName = serverName,
              remoteAddress = remoteAddress,
              attemptedConnect = attemptedConnect,
              enabledPermissions = enabledPermissionSummary,
              methodLabel = if (gatewayInputMode == GatewayInputMode.SetupCode) methodQr else methodManual,
            )
        }
      }

      Spacer(Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val backEnabled = step != OnboardingStep.Welcome
        Surface(
          modifier = Modifier.size(52.dp),
          shape = RoundedCornerShape(14.dp),
          color = onboardingSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, if (backEnabled) onboardingBorderStrong else onboardingBorder),
        ) {
          IconButton(
            onClick = {
              step =
                when (step) {
                  OnboardingStep.Welcome -> OnboardingStep.Welcome
                  OnboardingStep.Gateway -> OnboardingStep.Welcome
                  OnboardingStep.Permissions -> OnboardingStep.Gateway
                  OnboardingStep.FinalCheck -> OnboardingStep.Permissions
                }
            },
            enabled = backEnabled,
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.onboarding_back),
              tint = if (backEnabled) onboardingTextSecondary else onboardingTextTertiary,
            )
          }
        }

        when (step) {
          OnboardingStep.Welcome -> {
            Button(
              onClick = { step = OnboardingStep.Gateway },
              modifier = Modifier.weight(1f).height(52.dp),
              shape = RoundedCornerShape(14.dp),
              colors = onboardingPrimaryButtonColors(),
            ) {
              Text(stringResource(R.string.onboarding_next), style = onboardingHeadlineStyle.copy(fontWeight = FontWeight.Bold))
            }
          }
          OnboardingStep.Gateway -> {
            Button(
              onClick = {
                if (gatewayInputMode == GatewayInputMode.SetupCode) {
                  val parsedSetup = decodeGatewaySetupCode(setupCode)
                  if (parsedSetup == null) {
                    gatewayError = errScanFirst
                    return@Button
                  }
                  val parsedGateway = parseGatewayEndpoint(parsedSetup.url)
                  if (parsedGateway == null) {
                    gatewayError = errInvalidSetupUrl
                    return@Button
                  }
                  gatewayUrl = parsedSetup.url
                  viewModel.setGatewayBootstrapToken(parsedSetup.bootstrapToken.orEmpty())
                  val sharedToken = parsedSetup.token.orEmpty().trim()
                  val password = parsedSetup.password.orEmpty().trim()
                  if (sharedToken.isNotEmpty()) {
                    viewModel.setGatewayToken(sharedToken)
                  } else if (!parsedSetup.bootstrapToken.isNullOrBlank()) {
                    viewModel.setGatewayToken("")
                  }
                  gatewayPassword = password
                  if (password.isEmpty() && !parsedSetup.bootstrapToken.isNullOrBlank()) {
                    viewModel.setGatewayPassword("")
                  }
                } else {
                  val manualUrl = composeGatewayManualUrl(manualHost, manualPort, manualTls)
                  val parsedGateway = manualUrl?.let(::parseGatewayEndpoint)
                  if (parsedGateway == null) {
                    gatewayError = errManualInvalid
                    return@Button
                  }
                  gatewayUrl = parsedGateway.displayUrl
                  viewModel.setGatewayBootstrapToken("")
                }
                step = OnboardingStep.Permissions
              },
              modifier = Modifier.weight(1f).height(52.dp),
              shape = RoundedCornerShape(14.dp),
              colors = onboardingPrimaryButtonColors(),
            ) {
              Text(stringResource(R.string.onboarding_next), style = onboardingHeadlineStyle.copy(fontWeight = FontWeight.Bold))
            }
          }
          OnboardingStep.Permissions -> {
            Button(
              onClick = {
                viewModel.setCameraEnabled(enableCamera)
                viewModel.setLocationMode(if (enableLocation) LocationMode.WhileUsing else LocationMode.Off)
                proceedFromPermissions()
              },
              modifier = Modifier.weight(1f).height(52.dp),
              shape = RoundedCornerShape(14.dp),
              colors = onboardingPrimaryButtonColors(),
            ) {
              Text(stringResource(R.string.onboarding_next), style = onboardingHeadlineStyle.copy(fontWeight = FontWeight.Bold))
            }
          }
          OnboardingStep.FinalCheck -> {
            if (canFinishOnboarding) {
              Button(
                onClick = { viewModel.setOnboardingCompleted(true) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = onboardingPrimaryButtonColors(),
              ) {
                Text(stringResource(R.string.onboarding_finish), style = onboardingHeadlineStyle.copy(fontWeight = FontWeight.Bold))
              }
            } else {
              Button(
                onClick = {
                  val parsed = parseGatewayEndpoint(gatewayUrl)
                  if (parsed == null) {
                    step = OnboardingStep.Gateway
                    gatewayError = errInvalidGateway
                    return@Button
                  }
                  val token = persistedGatewayToken.trim()
                  val password = gatewayPassword.trim()
                  attemptedConnect = true
                  viewModel.setManualEnabled(true)
                  viewModel.setManualHost(parsed.host)
                  viewModel.setManualPort(parsed.port)
                  viewModel.setManualTls(parsed.tls)
                  if (gatewayInputMode == GatewayInputMode.Manual) {
                    viewModel.setGatewayBootstrapToken("")
                  }
                  if (token.isNotEmpty()) {
                    viewModel.setGatewayToken(token)
                  } else {
                    viewModel.setGatewayToken("")
                  }
                  viewModel.setGatewayPassword(password)
                  viewModel.connect(
                    GatewayEndpoint.manual(host = parsed.host, port = parsed.port),
                    token = token.ifEmpty { null },
                    bootstrapToken =
                      if (gatewayInputMode == GatewayInputMode.SetupCode) {
                        decodeGatewaySetupCode(setupCode)?.bootstrapToken?.trim()?.ifEmpty { null }
                      } else {
                        null
                      },
                    password = password.ifEmpty { null },
                  )
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = onboardingPrimaryButtonColors(),
              ) {
                Text(stringResource(R.string.onboarding_connect_btn), style = onboardingHeadlineStyle.copy(fontWeight = FontWeight.Bold))
              }
            }
          }
        }
      }
    }
  }
}

internal fun canFinishOnboarding(isConnected: Boolean, isNodeConnected: Boolean): Boolean {
  return isConnected && isNodeConnected
}

@Composable
private fun onboardingPrimaryButtonColors() =
  ButtonDefaults.buttonColors(
    containerColor = onboardingAccent,
    contentColor = Color.White,
    disabledContainerColor = onboardingAccent.copy(alpha = 0.45f),
    disabledContentColor = Color.White.copy(alpha = 0.9f),
  )

@Composable
private fun onboardingTextFieldColors() =
  OutlinedTextFieldDefaults.colors(
    focusedContainerColor = onboardingSurface,
    unfocusedContainerColor = onboardingSurface,
    focusedBorderColor = onboardingAccent,
    unfocusedBorderColor = onboardingBorder,
    focusedTextColor = onboardingText,
    unfocusedTextColor = onboardingText,
    cursorColor = onboardingAccent,
  )

@Composable
private fun onboardingSwitchColors() =
  SwitchDefaults.colors(
    checkedTrackColor = onboardingAccent,
    uncheckedTrackColor = onboardingBorderStrong,
    checkedThumbColor = Color.White,
    uncheckedThumbColor = Color.White,
  )

@Composable
private fun StepRail(current: OnboardingStep) {
  val steps = OnboardingStep.entries
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    steps.forEach { step ->
      val complete = step.index < current.index
      val active = step.index == current.index
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(5.dp)
              .background(
                color =
                  when {
                    complete -> onboardingSuccess
                    active -> onboardingAccent
                    else -> onboardingBorder
                  },
                shape = RoundedCornerShape(999.dp),
              ),
        )
        Text(
          text = step.localizedLabel(),
          style = onboardingCaption2Style.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
          color = if (active) onboardingAccent else onboardingTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun WelcomeStep() {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    FeatureCard(
      icon = Icons.Default.Wifi,
      title = stringResource(R.string.onboarding_feature_connect_title),
      subtitle = stringResource(R.string.onboarding_feature_connect_sub),
      accentColor = onboardingAccent,
    )
    FeatureCard(
      icon = Icons.Default.Tune,
      title = stringResource(R.string.onboarding_feature_perms_title),
      subtitle = stringResource(R.string.onboarding_feature_perms_sub),
      accentColor = Color(0xFF7C5AC7),
    )
    FeatureCard(
      icon = Icons.Default.ChatBubble,
      title = stringResource(R.string.onboarding_feature_chat_title),
      subtitle = stringResource(R.string.onboarding_feature_chat_sub),
      accentColor = onboardingSuccess,
    )
    FeatureCard(
      icon = Icons.Default.CheckCircle,
      title = stringResource(R.string.onboarding_feature_verify_title),
      subtitle = stringResource(R.string.onboarding_feature_verify_sub),
      accentColor = Color(0xFFC8841A),
    )
  }
}

@Composable
private fun GatewayStep(
  inputMode: GatewayInputMode,
  advancedOpen: Boolean,
  setupCode: String,
  manualHost: String,
  manualPort: String,
  manualTls: Boolean,
  gatewayToken: String,
  gatewayPassword: String,
  gatewayError: String?,
  onScanQrClick: () -> Unit,
  onAdvancedOpenChange: (Boolean) -> Unit,
  onInputModeChange: (GatewayInputMode) -> Unit,
  onSetupCodeChange: (String) -> Unit,
  onManualHostChange: (String) -> Unit,
  onManualPortChange: (String) -> Unit,
  onManualTlsChange: (Boolean) -> Unit,
  onTokenChange: (String) -> Unit,
  onPasswordChange: (String) -> Unit,
) {
  val resolvedEndpoint = remember(setupCode) { decodeGatewaySetupCode(setupCode)?.url?.let { parseGatewayEndpoint(it)?.displayUrl } }
  val manualResolvedEndpoint = remember(manualHost, manualPort, manualTls) { composeGatewayManualUrl(manualHost, manualPort, manualTls)?.let { parseGatewayEndpoint(it)?.displayUrl } }

  StepShell(title = stringResource(R.string.onboarding_gateway_title)) {
    Text(
      stringResource(R.string.onboarding_gateway_desc),
      style = onboardingCalloutStyle,
      color = onboardingTextSecondary,
    )
    CommandBlock("openclaw qr")
    Button(
      onClick = onScanQrClick,
      modifier = Modifier.fillMaxWidth().height(48.dp),
      shape = RoundedCornerShape(12.dp),
      colors = onboardingPrimaryButtonColors(),
    ) {
      Text(stringResource(R.string.onboarding_scan_qr), style = onboardingHeadlineStyle.copy(fontWeight = FontWeight.Bold))
    }
    if (!resolvedEndpoint.isNullOrBlank()) {
      Text(stringResource(R.string.onboarding_qr_captured), style = onboardingCalloutStyle, color = onboardingSuccess)
      ResolvedEndpoint(endpoint = resolvedEndpoint)
    }

    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      color = onboardingSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, onboardingBorderStrong),
      onClick = { onAdvancedOpenChange(!advancedOpen) },
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(stringResource(R.string.onboarding_advanced_title), style = onboardingHeadlineStyle, color = onboardingText)
          Text(stringResource(R.string.onboarding_advanced_subtitle), style = onboardingCaption1Style, color = onboardingTextSecondary)
        }
        Icon(
          imageVector = if (advancedOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = if (advancedOpen) stringResource(R.string.onboarding_advanced_collapse) else stringResource(R.string.onboarding_advanced_expand),
          tint = onboardingTextSecondary,
        )
      }
    }

    AnimatedVisibility(visible = advancedOpen) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GatewayModeToggle(inputMode = inputMode, onInputModeChange = onInputModeChange)

        if (inputMode == GatewayInputMode.SetupCode) {
          Text(stringResource(R.string.onboarding_setup_code_label), style = onboardingCaption1Style.copy(letterSpacing = 0.9.sp), color = onboardingTextSecondary)
          OutlinedTextField(
            value = setupCode,
            onValueChange = onSetupCodeChange,
            placeholder = { Text(stringResource(R.string.onboarding_placeholder_setup_code), color = onboardingTextTertiary, style = onboardingBodyStyle) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            textStyle = onboardingBodyStyle.copy(fontFamily = FontFamily.Monospace, color = onboardingText),
            shape = RoundedCornerShape(14.dp),
            colors =
              onboardingTextFieldColors(),
          )
          if (!resolvedEndpoint.isNullOrBlank()) {
            ResolvedEndpoint(endpoint = resolvedEndpoint)
          }
        } else {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickFillChip(label = stringResource(R.string.quickfill_android_emulator), onClick = {
              onManualHostChange("10.0.2.2")
              onManualPortChange("18789")
              onManualTlsChange(false)
            })
            QuickFillChip(label = stringResource(R.string.quickfill_localhost), onClick = {
              onManualHostChange("127.0.0.1")
              onManualPortChange("18789")
              onManualTlsChange(false)
            })
          }

          Text(stringResource(R.string.onboarding_host_label), style = onboardingCaption1Style.copy(letterSpacing = 0.9.sp), color = onboardingTextSecondary)
          OutlinedTextField(
            value = manualHost,
            onValueChange = onManualHostChange,
            placeholder = { Text("10.0.2.2", color = onboardingTextTertiary, style = onboardingBodyStyle) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            textStyle = onboardingBodyStyle.copy(color = onboardingText),
            shape = RoundedCornerShape(14.dp),
            colors =
              onboardingTextFieldColors(),
          )

          Text(stringResource(R.string.onboarding_port_label), style = onboardingCaption1Style.copy(letterSpacing = 0.9.sp), color = onboardingTextSecondary)
          OutlinedTextField(
            value = manualPort,
            onValueChange = onManualPortChange,
            placeholder = { Text("18789", color = onboardingTextTertiary, style = onboardingBodyStyle) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = onboardingBodyStyle.copy(fontFamily = FontFamily.Monospace, color = onboardingText),
            shape = RoundedCornerShape(14.dp),
            colors =
              onboardingTextFieldColors(),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(stringResource(R.string.use_tls), style = onboardingHeadlineStyle, color = onboardingText)
              Text(stringResource(R.string.use_tls_subtitle), style = onboardingCalloutStyle.copy(lineHeight = 18.sp), color = onboardingTextSecondary)
            }
            Switch(
              checked = manualTls,
              onCheckedChange = onManualTlsChange,
              colors =
                onboardingSwitchColors(),
            )
          }

          Text(stringResource(R.string.onboarding_token_label), style = onboardingCaption1Style.copy(letterSpacing = 0.9.sp), color = onboardingTextSecondary)
          OutlinedTextField(
            value = gatewayToken,
            onValueChange = onTokenChange,
            placeholder = { Text(stringResource(R.string.placeholder_token), color = onboardingTextTertiary, style = onboardingBodyStyle) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            textStyle = onboardingBodyStyle.copy(color = onboardingText),
            shape = RoundedCornerShape(14.dp),
            colors =
              onboardingTextFieldColors(),
          )

          Text(stringResource(R.string.onboarding_password_label), style = onboardingCaption1Style.copy(letterSpacing = 0.9.sp), color = onboardingTextSecondary)
          OutlinedTextField(
            value = gatewayPassword,
            onValueChange = onPasswordChange,
            placeholder = { Text(stringResource(R.string.placeholder_password), color = onboardingTextTertiary, style = onboardingBodyStyle) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            textStyle = onboardingBodyStyle.copy(color = onboardingText),
            shape = RoundedCornerShape(14.dp),
            colors =
              onboardingTextFieldColors(),
          )

          if (!manualResolvedEndpoint.isNullOrBlank()) {
            ResolvedEndpoint(endpoint = manualResolvedEndpoint)
          }
        }
      }
    }

    if (!gatewayError.isNullOrBlank()) {
      Text(gatewayError, color = onboardingWarning, style = onboardingCaption1Style)
    }
  }
}

@Composable
private fun GuideBlock(
  title: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(onboardingAccent.copy(alpha = 0.4f)))
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(title, style = onboardingHeadlineStyle, color = onboardingText)
      content()
    }
  }
}

@Composable
private fun GatewayModeToggle(
  inputMode: GatewayInputMode,
  onInputModeChange: (GatewayInputMode) -> Unit,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
    GatewayModeChip(
      label = stringResource(R.string.setup_code),
      active = inputMode == GatewayInputMode.SetupCode,
      onClick = { onInputModeChange(GatewayInputMode.SetupCode) },
      modifier = Modifier.weight(1f),
    )
    GatewayModeChip(
      label = stringResource(R.string.manual),
      active = inputMode == GatewayInputMode.Manual,
      onClick = { onInputModeChange(GatewayInputMode.Manual) },
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun GatewayModeChip(
  label: String,
  active: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Button(
    onClick = onClick,
    modifier = modifier.height(40.dp),
    shape = RoundedCornerShape(12.dp),
    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = if (active) onboardingAccent else onboardingSurface,
        contentColor = if (active) Color.White else onboardingText,
      ),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (active) onboardingAccentBorderStrong else onboardingBorderStrong),
  ) {
    Text(
      text = label,
      style = onboardingCaption1Style.copy(fontWeight = FontWeight.Bold),
    )
  }
}

@Composable
private fun QuickFillChip(
  label: String,
  onClick: () -> Unit,
) {
  TextButton(
    onClick = onClick,
    shape = RoundedCornerShape(999.dp),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
    colors =
      ButtonDefaults.textButtonColors(
        containerColor = onboardingAccentSoft,
        contentColor = onboardingAccent,
      ),
  ) {
    Text(label, style = onboardingCaption1Style.copy(fontWeight = FontWeight.SemiBold))
  }
}

@Composable
private fun ResolvedEndpoint(endpoint: String) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    HorizontalDivider(color = onboardingBorder)
    Text(
      stringResource(R.string.onboarding_resolved_endpoint),
      style = onboardingCaption2Style.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.7.sp),
      color = onboardingTextSecondary,
    )
    Text(
      endpoint,
      style = onboardingCalloutStyle.copy(fontFamily = FontFamily.Monospace),
      color = onboardingText,
    )
    HorizontalDivider(color = onboardingBorder)
  }
}

@Composable
private fun StepShell(
  title: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(modifier = Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(title, style = onboardingTitle1Style, color = onboardingText)
    content()
  }
}

@Composable
private fun InlineDivider() {
  HorizontalDivider(color = onboardingBorder)
}

@Composable
private fun PermissionsStep(
  enableDiscovery: Boolean,
  enableLocation: Boolean,
  enableNotifications: Boolean,
  enableNotificationListener: Boolean,
  enableMicrophone: Boolean,
  enableCamera: Boolean,
  enablePhotos: Boolean,
  enableContacts: Boolean,
  enableCalendar: Boolean,
  enableMotion: Boolean,
  motionAvailable: Boolean,
  motionPermissionRequired: Boolean,
  enableSms: Boolean,
  smsAvailable: Boolean,
  callLogAvailable: Boolean,
  enableCallLog: Boolean,
  context: Context,
  onDiscoveryChange: (Boolean) -> Unit,
  onLocationChange: (Boolean) -> Unit,
  onNotificationsChange: (Boolean) -> Unit,
  onNotificationListenerChange: (Boolean) -> Unit,
  onMicrophoneChange: (Boolean) -> Unit,
  onCameraChange: (Boolean) -> Unit,
  onPhotosChange: (Boolean) -> Unit,
  onContactsChange: (Boolean) -> Unit,
  onCalendarChange: (Boolean) -> Unit,
  onMotionChange: (Boolean) -> Unit,
  onSmsChange: (Boolean) -> Unit,
  onCallLogChange: (Boolean) -> Unit,
) {
  val discoveryPermission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.NEARBY_WIFI_DEVICES else Manifest.permission.ACCESS_FINE_LOCATION
  val locationGranted =
    isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
      isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
  val photosPermission =
    if (Build.VERSION.SDK_INT >= 33) {
      Manifest.permission.READ_MEDIA_IMAGES
    } else {
      Manifest.permission.READ_EXTERNAL_STORAGE
    }
  val contactsGranted =
    isPermissionGranted(context, Manifest.permission.READ_CONTACTS) &&
      isPermissionGranted(context, Manifest.permission.WRITE_CONTACTS)
  val calendarGranted =
    isPermissionGranted(context, Manifest.permission.READ_CALENDAR) &&
      isPermissionGranted(context, Manifest.permission.WRITE_CALENDAR)
  val motionGranted =
    if (!motionAvailable) {
      false
    } else if (!motionPermissionRequired) {
      true
    } else {
      isPermissionGranted(context, Manifest.permission.ACTIVITY_RECOGNITION)
    }
  val notificationListenerGranted = isNotificationListenerEnabled(context)

  StepShell(title = stringResource(R.string.onboarding_perms_title)) {
    Text(
      stringResource(R.string.onboarding_perms_desc),
      style = onboardingCalloutStyle,
      color = onboardingTextSecondary,
    )

    PermissionSectionHeader(stringResource(R.string.onboarding_perm_section_system))
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_discovery),
      subtitle = stringResource(R.string.onboarding_perm_discovery_desc),
      checked = enableDiscovery,
      granted = isPermissionGranted(context, discoveryPermission),
      onCheckedChange = onDiscoveryChange,
    )
    InlineDivider()
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_location),
      subtitle = stringResource(R.string.onboarding_perm_location_desc),
      checked = enableLocation,
      granted = locationGranted,
      onCheckedChange = onLocationChange,
    )
    InlineDivider()
    if (Build.VERSION.SDK_INT >= 33) {
      PermissionToggleRow(
        title = stringResource(R.string.onboarding_perm_notifications),
        subtitle = stringResource(R.string.onboarding_perm_notifications_desc),
        checked = enableNotifications,
        granted = isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS),
        onCheckedChange = onNotificationsChange,
      )
      InlineDivider()
    }
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_notif_listener),
      subtitle = stringResource(R.string.onboarding_perm_notif_listener_desc),
      checked = enableNotificationListener,
      granted = notificationListenerGranted,
      onCheckedChange = onNotificationListenerChange,
    )

    PermissionSectionHeader(stringResource(R.string.onboarding_perm_section_media))
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_microphone),
      subtitle = stringResource(R.string.onboarding_perm_microphone_desc),
      checked = enableMicrophone,
      granted = isPermissionGranted(context, Manifest.permission.RECORD_AUDIO),
      onCheckedChange = onMicrophoneChange,
    )
    InlineDivider()
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_camera),
      subtitle = stringResource(R.string.onboarding_perm_camera_desc),
      checked = enableCamera,
      granted = isPermissionGranted(context, Manifest.permission.CAMERA),
      onCheckedChange = onCameraChange,
    )
    InlineDivider()
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_photos),
      subtitle = stringResource(R.string.onboarding_perm_photos_desc),
      checked = enablePhotos,
      granted = isPermissionGranted(context, photosPermission),
      onCheckedChange = onPhotosChange,
    )

    PermissionSectionHeader(stringResource(R.string.onboarding_perm_section_personal))
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_contacts),
      subtitle = stringResource(R.string.onboarding_perm_contacts_desc),
      checked = enableContacts,
      granted = contactsGranted,
      onCheckedChange = onContactsChange,
    )
    InlineDivider()
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_calendar),
      subtitle = stringResource(R.string.onboarding_perm_calendar_desc),
      checked = enableCalendar,
      granted = calendarGranted,
      onCheckedChange = onCalendarChange,
    )
    InlineDivider()
    PermissionToggleRow(
      title = stringResource(R.string.onboarding_perm_motion),
      subtitle = stringResource(R.string.onboarding_perm_motion_desc),
      checked = enableMotion,
      granted = motionGranted,
      onCheckedChange = onMotionChange,
      enabled = motionAvailable,
      statusOverride = if (!motionAvailable) stringResource(R.string.onboarding_perm_motion_unavailable) else null,
    )
    if (smsAvailable) {
      InlineDivider()
      PermissionToggleRow(
        title = stringResource(R.string.onboarding_perm_sms),
        subtitle = stringResource(R.string.onboarding_perm_sms_desc),
        checked = enableSms,
        granted =
          isPermissionGranted(context, Manifest.permission.SEND_SMS) ||
            isPermissionGranted(context, Manifest.permission.READ_SMS),
        onCheckedChange = onSmsChange,
      )
    }
    if (callLogAvailable) {
      InlineDivider()
      PermissionToggleRow(
        title = stringResource(R.string.onboarding_perm_call_log),
        subtitle = stringResource(R.string.onboarding_perm_call_log_desc),
        checked = enableCallLog,
        granted = isPermissionGranted(context, Manifest.permission.READ_CALL_LOG),
        onCheckedChange = onCallLogChange,
      )
    }
    Text(stringResource(R.string.onboarding_perms_footer), style = onboardingCalloutStyle, color = onboardingTextSecondary)
  }
}

@Composable
private fun PermissionSectionHeader(title: String) {
  Text(
    title.uppercase(),
    style = onboardingCaption1Style.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
    color = onboardingAccent,
    modifier = Modifier.padding(top = 8.dp),
  )
}

@Composable
private fun PermissionToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  granted: Boolean,
  enabled: Boolean = true,
  statusOverride: String? = null,
  onCheckedChange: (Boolean) -> Unit,
) {
  val statusText = statusOverride ?: if (granted) stringResource(R.string.granted) else stringResource(R.string.not_granted)
  val statusColor = when {
    statusOverride != null -> onboardingTextTertiary
    granted -> onboardingSuccess
    else -> onboardingWarning
  }
  Row(
    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(title, style = onboardingHeadlineStyle, color = onboardingText)
      Text(subtitle, style = onboardingCalloutStyle.copy(lineHeight = 18.sp), color = onboardingTextSecondary)
      Text(statusText, style = onboardingCaption1Style, color = statusColor)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      colors = onboardingSwitchColors(),
    )
  }
}

@Composable
private fun FinalStep(
  parsedGateway: GatewayEndpointConfig?,
  statusText: String,
  isConnected: Boolean,
  serverName: String?,
  remoteAddress: String?,
  attemptedConnect: Boolean,
  enabledPermissions: String,
  methodLabel: String,
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val gatewayAddress = parsedGateway?.displayUrl ?: "Invalid gateway URL"
  val statusLabel = gatewayStatusForDisplay(statusText)
  val showDiagnostics = gatewayStatusHasDiagnostics(statusText)
  val pairingRequired = gatewayStatusLooksLikePairing(statusText)

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text(stringResource(R.string.onboarding_final_title), style = onboardingTitle1Style, color = onboardingText)

    SummaryCard(
      icon = Icons.Default.Link,
      label = stringResource(R.string.onboarding_final_method_label),
      value = methodLabel,
      accentColor = onboardingAccent,
    )
    SummaryCard(
      icon = Icons.Default.Cloud,
      label = stringResource(R.string.onboarding_final_gateway_label),
      value = parsedGateway?.displayUrl ?: stringResource(R.string.onboarding_final_invalid_gateway),
      accentColor = Color(0xFF7C5AC7),
    )
    SummaryCard(
      icon = Icons.Default.Security,
      label = stringResource(R.string.onboarding_final_perms_label),
      value = enabledPermissions,
      accentColor = onboardingSuccess,
    )

    if (!attemptedConnect) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = onboardingAccentSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, onboardingAccent.copy(alpha = 0.2f)),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier
                .size(42.dp)
                .background(onboardingAccent.copy(alpha = 0.1f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Wifi,
              contentDescription = null,
              tint = onboardingAccent,
              modifier = Modifier.size(22.dp),
            )
          }
          Text(
            stringResource(R.string.onboarding_final_tap_connect),
            style = onboardingCalloutStyle,
            color = onboardingAccent,
          )
        }
      }
    } else if (isConnected) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = onboardingSuccessSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, onboardingSuccess.copy(alpha = 0.2f)),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier
                .size(42.dp)
                .background(onboardingSuccess.copy(alpha = 0.1f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = onboardingSuccess,
              modifier = Modifier.size(22.dp),
            )
          }
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.onboarding_final_connected), style = onboardingHeadlineStyle, color = onboardingSuccess)
            Text(
              serverName ?: remoteAddress ?: "gateway",
              style = onboardingCalloutStyle,
              color = onboardingSuccess.copy(alpha = 0.8f),
            )
          }
        }
      }
    } else {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = onboardingWarningSoft,
        border = BorderStroke(1.dp, onboardingWarning.copy(alpha = 0.2f)),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier =
                Modifier
                  .size(42.dp)
                  .background(onboardingWarning.copy(alpha = 0.1f), RoundedCornerShape(11.dp)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = onboardingWarning,
                modifier = Modifier.size(22.dp),
              )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                  if (pairingRequired) stringResource(R.string.onboarding_final_pairing_required) else stringResource(R.string.onboarding_final_connection_failed),
                  style = onboardingHeadlineStyle,
                  color = onboardingWarning,
              )
              Text(
                  if (pairingRequired) {
                    stringResource(R.string.onboarding_final_run_on_host)
                  } else {
                    stringResource(R.string.onboarding_final_copy_report)
                  },
                  style = onboardingCalloutStyle,
                  color = onboardingTextSecondary,
              )
            }
          }
          Text("Status", style = onboardingCaption1Style.copy(fontWeight = FontWeight.Bold), color = onboardingTextSecondary)
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = onboardingCommandBg,
            border = BorderStroke(1.dp, onboardingCommandBorder),
          ) {
            Text(
              statusLabel,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              style = onboardingCalloutStyle.copy(fontFamily = FontFamily.Monospace),
              color = onboardingCommandText,
            )
          }
          if (showDiagnostics) {
            Text(stringResource(R.string.onboarding_final_diag_error_label), style = onboardingCaption1Style.copy(fontWeight = FontWeight.Bold), color = onboardingTextSecondary)
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = onboardingCommandBg,
              border = BorderStroke(1.dp, onboardingCommandBorder),
            ) {
              Text(
                statusLabel,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                style = onboardingCalloutStyle.copy(fontFamily = FontFamily.Monospace),
                color = onboardingCommandText,
              )
            }
            Text(
              "OpenClaw Android ${openClawAndroidVersionLabel()}",
              style = onboardingCaption1Style,
              color = onboardingTextSecondary,
            )
            Button(
              onClick = {
                copyGatewayDiagnosticsReport(
                  context = context,
                  screen = "onboarding final check",
                  gatewayAddress = gatewayAddress,
                  statusText = statusLabel,
                )
              },
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = onboardingSurface, contentColor = onboardingWarning),
              border = BorderStroke(1.dp, onboardingWarning.copy(alpha = 0.3f)),
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(stringResource(R.string.onboarding_final_copy_report), style = onboardingCalloutStyle.copy(fontWeight = FontWeight.Bold))
            }
          }
          if (pairingRequired) {
            CommandBlock("openclaw devices list")
            CommandBlock("openclaw devices approve <requestId>")
            Text(stringResource(R.string.onboarding_final_tap_again), style = onboardingCalloutStyle, color = onboardingTextSecondary)
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryCard(
  icon: ImageVector,
  label: String,
  value: String,
  accentColor: Color,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = onboardingSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, onboardingBorder),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.Top,
    ) {
      Box(
        modifier =
          Modifier
            .size(42.dp)
            .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(22.dp),
        )
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          label.uppercase(),
          style = onboardingCaption1Style.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
          color = onboardingTextSecondary,
        )
        Text(value, style = onboardingHeadlineStyle, color = onboardingText)
      }
    }
  }
}

@Composable
private fun CommandBlock(command: String) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
        .clip(RoundedCornerShape(12.dp))
        .background(onboardingCommandBg)
        .border(width = 1.dp, color = onboardingCommandBorder, shape = RoundedCornerShape(12.dp)),
  ) {
    Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(onboardingCommandAccent))
    Text(
      command,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      style = onboardingCalloutStyle,
      fontFamily = FontFamily.Monospace,
      color = onboardingCommandText,
    )
  }
}

@Composable
private fun FeatureCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  accentColor: Color,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = onboardingSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, onboardingBorder),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(42.dp)
            .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(22.dp),
        )
      }
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = onboardingHeadlineStyle, color = onboardingText)
        Text(subtitle, style = onboardingCalloutStyle, color = onboardingTextSecondary)
      }
    }
  }
}

private fun isPermissionGranted(context: Context, permission: String): Boolean {
  return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
  return DeviceNotificationListenerService.isAccessEnabled(context)
}

private fun openNotificationListenerSettings(context: Context) {
  val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  runCatching {
    context.startActivity(intent)
  }.getOrElse {
    openAppSettings(context)
  }
}

private fun openAppSettings(context: Context) {
  val intent =
    Intent(
      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
      Uri.fromParts("package", context.packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  context.startActivity(intent)
}

private fun hasMotionCapabilities(context: Context): Boolean {
  val sensorManager = context.getSystemService(SensorManager::class.java) ?: return false
  return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null ||
    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
}
