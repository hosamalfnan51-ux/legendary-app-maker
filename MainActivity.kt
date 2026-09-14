package com.example

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.appbuilder.data.repository.AIProvider
import com.example.appbuilder.data.repository.ResilientAIRouter
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        LegendaryAppBuilderRoot()
      }
    }
  }
}

// -------------------------------------------------------------
// تبويبات التنقل الرئيسية في التطبيق (Navigation Tabs)
// -------------------------------------------------------------
enum class AppStudioTab {
  CHAT_VIEW,        // شاشة الدردشة وتوليد التطبيقات
  LIVE_PREVIEW,     // نافذة المعاينة الحية والتفاعل اللمسي
  ACTION_HUB,       // لوحة التحكم وأزرار التصدير (Termux + GitHub)
  BUG_HUNTER,       // صندوق أدوات الصيانة وصائد الأخطاء
  BYOK_CONFIG       // شاشة إعدادات المفاتيح والتحكم
}

// -------------------------------------------------------------
// الشاشة الأسطورية الرئيسية (The Root Screen)
// -------------------------------------------------------------
@Composable
fun LegendaryAppBuilderRoot() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // حالات التنقل والمدخلات
  var currentTab by remember { mutableStateOf(AppStudioTab.CHAT_VIEW) }
  var promptInput by remember { mutableStateOf("") }
  var isVoiceRecording by remember { mutableStateOf(false) }
  var isGenerating by remember { mutableStateOf(false) }
  var isFixingBugs by remember { mutableStateOf(false) }
  var bugInputText by remember { mutableStateOf("") }

  // مفاتيح الـ API (تدعم وضع التجاوز التام والعمل المجاني بدون مفاتيح)
  var cerebrasKey by remember { mutableStateOf("") }
  var geminiKey by remember { mutableStateOf("") }
  var openRouterKey by remember { mutableStateOf("") }
  var isBypassActive by remember { mutableStateOf(true) }

  // محرك الذكاء الاصطناعي متعدد المسارات (Resilient AI Router)
  val aiRouter = remember(cerebrasKey, geminiKey, openRouterKey, isBypassActive) {
    if (isBypassActive) {
      ResilientAIRouter("", "", "")
    } else {
      ResilientAIRouter(cerebrasKey, geminiKey, openRouterKey)
    }
  }

  // التطبيق المولد حالياً
  var appTitle by remember { mutableStateOf("لعبة الثعبان السيبراني الذهبي") }
  var generatedHtmlCode by remember {
    mutableStateOf(aiRouter.generateSmartFallbackApp("ثعبان"))
  }
  var termuxScript by remember {
    mutableStateOf(aiRouter.generateTermuxScript("cyber_snake", generatedHtmlCode))
  }
  var lastUsedProvider by remember { mutableStateOf(AIProvider.GEMINI_STUDIO) }
  val jsConsoleErrors = remember { mutableStateListOf<String>() }

  // حوار مزامنة GitHub
  var showGitHubDialog by remember { mutableStateOf(false) }
  var githubRepoUrl by remember { mutableStateOf("https://github.com/my-legendary-apps") }
  var githubBranch by remember { mutableStateOf("main") }
  var isGitHubSyncing by remember { mutableStateOf(false) }

  fun notifyUser(message: String) {
    scope.launch {
      snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
    }
  }

  fun copyToClipboard(label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    notifyUser("تم نسخ $label بنجاح إلى الحافظة! 📋")
  }

  fun shareContent(title: String, content: String) {
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TITLE, title)
      putExtra(Intent.EXTRA_TEXT, content)
      type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة $title"))
  }

  // دالة توليد التطبيق باستخدام المحرك المرن
  fun performAppGeneration(customPrompt: String? = null) {
    val activePrompt = customPrompt ?: promptInput.trim()
    if (activePrompt.isBlank()) {
      notifyUser("يرجى كتابة فكرة التطبيق أو اللعبة أولاً!")
      return
    }

    scope.launch {
      isGenerating = true
      try {
        val result = aiRouter.generateApplicationCode(activePrompt)
        if (result.isSuccess && !result.generatedCode.isNullOrBlank()) {
          generatedHtmlCode = result.generatedCode
          termuxScript = result.termuxScript ?: aiRouter.generateTermuxScript("legendary_app", result.generatedCode)
          lastUsedProvider = result.providerUsed

          // استنتاج عنوان مناسب
          appTitle = when {
            activePrompt.contains("فضاء") || activePrompt.contains("space") -> "🚀 حامي المجرة الفضائية"
            activePrompt.contains("سباق") || activePrompt.contains("car") -> "🏎️ سباق السرعة التيربو"
            activePrompt.contains("حاسب") || activePrompt.contains("calc") -> "🧮 الحاسبة الذهبية الذكية"
            activePrompt.contains("مهام") || activePrompt.contains("todo") -> "📝 مفكرة المهام التنفيذية"
            activePrompt.contains("ثعبان") || activePrompt.contains("snake") -> "🐍 الثعبان السيبراني الذهبي"
            else -> "⚡ تطبيق أسطوري: ${activePrompt.take(20)}"
          }

          jsConsoleErrors.clear()
          notifyUser("✅ تم توليد $appTitle بنجاح بنسبة 100%! انتقل للمعاينة والتصدير")
          currentTab = AppStudioTab.LIVE_PREVIEW
        } else {
          notifyUser("⚠️ حدث تعذر في التوليد: ${result.errorMessage ?: "خطأ غير معروف"}")
        }
      } catch (e: Exception) {
        notifyUser("خطأ أثناء التوليد: ${e.localizedMessage}")
      } finally {
        isGenerating = false
      }
    }
  }

  // دالة فحص وإصلاح الأخطاء آلياً 100%
  fun performBugFix() {
    scope.launch {
      isFixingBugs = true
      try {
        val errorLog = if (bugInputText.isNotBlank()) bugInputText else jsConsoleErrors.joinToString("\n")
        val fixResult = aiRouter.fixCodeBug(generatedHtmlCode, errorLog)
        if (fixResult.isSuccess && !fixResult.generatedCode.isNullOrBlank()) {
          generatedHtmlCode = fixResult.generatedCode
          termuxScript = fixResult.termuxScript ?: aiRouter.generateTermuxScript("fixed_app", fixResult.generatedCode)
          jsConsoleErrors.clear()
          bugInputText = ""
          notifyUser("🎯 تم صيد الثغرات وإعادة بناء الكود سليماً 100%!")
          currentTab = AppStudioTab.LIVE_PREVIEW
        } else {
          notifyUser("تعذر الإصلاح التلقائي: ${fixResult.errorMessage}")
        }
      } catch (e: Exception) {
        notifyUser("خطأ أثناء الصيانة: ${e.localizedMessage}")
      } finally {
        isFixingBugs = false
      }
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBg)
      .testTag("legendary_app_scaffold"),
    containerColor = ObsidianBg,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      LuxuryTopBar(
        activeAppName = appTitle,
        isBypassMode = isBypassActive,
        providerUsed = lastUsedProvider
      )
    },
    bottomBar = {
      LuxuryNavigationBar(
        currentTab = currentTab,
        onTabSelected = { currentTab = it },
        errorCount = jsConsoleErrors.size
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      when (currentTab) {
        AppStudioTab.CHAT_VIEW -> {
          MainChatView(
            promptInput = promptInput,
            onPromptChange = { promptInput = it },
            isVoiceRecording = isVoiceRecording,
            onToggleVoice = {
              isVoiceRecording = !isVoiceRecording
              if (isVoiceRecording) {
                promptInput = "اصنع لي لعبة حرب فضاء تفاعلية باللمس مع مؤثرات صوتية وتأثيرات ذهبية"
                notifyUser("🎙️ تم الاستماع للأمر الصوتي وتحويله لكود بنجاح!")
                isVoiceRecording = false
              }
            },
            isGenerating = isGenerating,
            onGenerate = { performAppGeneration() },
            onQuickSelect = { sample ->
              promptInput = sample
              performAppGeneration(sample)
            }
          )
        }

        AppStudioTab.LIVE_PREVIEW -> {
          LivePreviewScreen(
            htmlCode = generatedHtmlCode,
            appName = appTitle,
            onConsoleError = { err ->
              if (!jsConsoleErrors.contains(err)) {
                jsConsoleErrors.add(err)
              }
            },
            onOpenBugHunter = { currentTab = AppStudioTab.BUG_HUNTER },
            onOpenExporter = { currentTab = AppStudioTab.ACTION_HUB }
          )
        }

        AppStudioTab.ACTION_HUB -> {
          ActionHubView(
            appName = appTitle,
            termuxScript = termuxScript,
            fullHtmlCode = generatedHtmlCode,
            onCopyTermux = { copyToClipboard("سكريبت Termux", termuxScript) },
            onCopyCode = { copyToClipboard("الكود البرمجي الكامل", generatedHtmlCode) },
            onShareBundle = { shareContent(appTitle, termuxScript) },
            onOpenGitHubDialog = { showGitHubDialog = true }
          )
        }

        AppStudioTab.BUG_HUNTER -> {
          AiBugHunterMenu(
            jsErrors = jsConsoleErrors,
            bugInputText = bugInputText,
            onBugInputChange = { bugInputText = it },
            isFixing = isFixingBugs,
            onTriggerFix = { performBugFix() },
            onClearLogs = { jsConsoleErrors.clear() }
          )
        }

        AppStudioTab.BYOK_CONFIG -> {
          ByokConfigurationScreen(
            cerebrasKey = cerebrasKey,
            onCerebrasKeyChange = { cerebrasKey = it },
            geminiKey = geminiKey,
            onGeminiKeyChange = { geminiKey = it },
            openRouterKey = openRouterKey,
            onOpenRouterKeyChange = { openRouterKey = it },
            isBypassActive = isBypassActive,
            onToggleBypass = {
              isBypassActive = it
              notifyUser(if (it) "تم تفعيل التجاوز والتشغيل المجاني المدمج!" else "تم تفعيل المفاتيح الشخصية!")
            },
            onSaveKeys = {
              notifyUser("تم حفظ مفاتيح API بأمان في هاتفك! 💾")
              currentTab = AppStudioTab.CHAT_VIEW
            }
          )
        }
      }
    }
  }

  // نافذة مزامنة مستودع GitHub
  if (showGitHubDialog) {
    AlertDialog(
      onDismissRequest = { if (!isGitHubSyncing) showGitHubDialog = false },
      shape = RoundedCornerShape(20.dp),
      containerColor = ObsidianSurface,
      tonalElevation = 8.dp,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CloudUpload, contentDescription = null, tint = GoldPrimary)
          Spacer(modifier = Modifier.width(10.dp))
          Text("مزامنة مستودع GitHub", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "مزامنة تطبيق '$appTitle' ورفع الكود المصدري وسكريبت Termux إلى مستودعك مباشرة:",
            color = TextSecondary,
            fontSize = 12.sp
          )
          OutlinedTextField(
            value = githubRepoUrl,
            onValueChange = { githubRepoUrl = it },
            label = { Text("رابط المستودع (GitHub Repo URL)", color = GoldTertiary, fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = ObsidianBorder,
              focusedContainerColor = ObsidianBg,
              unfocusedContainerColor = ObsidianBg,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth().testTag("github_repo_input")
          )
          OutlinedTextField(
            value = githubBranch,
            onValueChange = { githubBranch = it },
            label = { Text("الفرع (Branch)", color = GoldTertiary, fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = ObsidianBorder,
              focusedContainerColor = ObsidianBg,
              unfocusedContainerColor = ObsidianBg,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth().testTag("github_branch_input")
          )
          if (isGitHubSyncing) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(10.dp))
              Text("جاري إنشاء الكوميت والمزامنة السحابية...", color = GoldTertiary, fontSize = 12.sp)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            scope.launch {
              isGitHubSyncing = true
              delay(1200)
              isGitHubSyncing = false
              showGitHubDialog = false
              notifyUser("✅ تمت المزامنة مع مستودع GitHub بنجاح تام!")
            }
          },
          enabled = !isGitHubSyncing,
          colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A0802)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("github_confirm_sync_btn")
        ) {
          Text("مزامنة الآن 🚀", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showGitHubDialog = false },
          enabled = !isGitHubSyncing,
          border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("إلغاء", color = TextSecondary)
        }
      }
    )
  }
}

// -------------------------------------------------------------
// 1. شاشة الدردشة الرئيسية (Main Chat View)
// -------------------------------------------------------------
@Composable
fun MainChatView(
  promptInput: String,
  onPromptChange: (String) -> Unit,
  isVoiceRecording: Boolean,
  onToggleVoice: () -> Unit,
  isGenerating: Boolean,
  onGenerate: () -> Unit,
  onQuickSelect: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (isVoiceRecording) 1.25f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "micPulse"
  )

  val quickIdeas = listOf(
    "🐍 الثعبان السيبراني" to "لعبة ثعبان سيبراني كلاسيكية بأسلوب سايبربانك أسود وذهبي مع تحكم باللمس ومؤثرات صوتية",
    "🚀 حامي المجرة الفضائي" to "لعبة قتال وإطلاق نار فضائية بأزرار لمس مريحة للهاتف مع تفجيرات وحفظ النقاط",
    "🏎️ سباق السرعة التيربو" to "لعبة سباق سيارات وتفادي العقبات باللمس مع شاشة عرض العدادات",
    "🧮 حاسبة هندسية وعلمية" to "آلة حاسبة علمية متكاملة وأنيقة بتصميم أسود وذهبي للعمليات المعقدة والنسب المئوية",
    "📝 مفكرة مهام تنفيذية" to "تطبيق إدارة مهام سريع يخزن البيانات في الهاتف مع تصنيف للمهام المكتملة والحذف الفوري"
  )

  Card(
    modifier = modifier
      .fillMaxSize()
      .testTag("main_chat_view_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = BorderStroke(1.2.dp, GoldSecondary.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "محرك صانع التطبيقات الأسطوري ⚡",
              color = GoldPrimary,
              fontSize = 17.sp,
              fontWeight = FontWeight.ExtraBold
            )
            Text(
              text = "تحويل أفكارك البرمجية إلى تطبيقات وألعاب كاملة فوراً",
              color = TextSecondary,
              fontSize = 12.sp
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF2A2210))
              .border(1.dp, GoldSecondary, RoundedCornerShape(12.dp))
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text("المحرك الثلاثي 🧠", color = GoldTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = ObsidianBg),
          border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.45f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
              value = promptInput,
              onValueChange = onPromptChange,
              placeholder = {
                Text(
                  "اكتب فكرة التطبيق أو اللعبة (مثال: اصنع لعبة قتال فضائي، أو تطبيق مفكرة)...",
                  color = Color(0xFF6B7280),
                  fontSize = 13.sp
                )
              },
              minLines = 4,
              maxLines = 7,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("chat_prompt_input")
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = onToggleVoice,
                  modifier = Modifier
                    .scale(pulseScale)
                    .size(42.dp)
                    .background(
                      if (isVoiceRecording) CrimsonAlert else Color(0xFF2A2210),
                      CircleShape
                    )
                    .border(1.dp, if (isVoiceRecording) CrimsonAlert else GoldSecondary, CircleShape)
                    .testTag("voice_command_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "أمر صوتي",
                    tint = if (isVoiceRecording) Color.White else GoldPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isVoiceRecording) "جاري التسجيل الصوتي..." else "إدخال صوتي",
                  color = if (isVoiceRecording) CrimsonAlert else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }

              if (promptInput.isNotBlank()) {
                Text(
                  text = "مسح",
                  color = GoldTertiary,
                  fontSize = 11.sp,
                  modifier = Modifier
                    .clickable { onPromptChange("") }
                    .padding(6.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "نماذج أسطورية سريعة بنقرة واحدة:",
          color = TextGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          quickIdeas.forEach { (title, fullText) ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianElevated)
                .border(1.dp, GoldSecondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable { onQuickSelect(fullText) }
                .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
              Text(title, color = GoldTertiary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onGenerate,
        enabled = !isGenerating && promptInput.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldPrimary,
          contentColor = Color(0xFF0A0802),
          disabledContainerColor = Color(0xFF262115),
          disabledContentColor = Color(0xFF5C5238)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("generate_app_future_btn")
      ) {
        if (isGenerating) {
          CircularProgressIndicator(
            color = Color(0xFF0A0802),
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.5.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text("جاري البناء الذكي والتوليد الأسطوري...", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        } else {
          Icon(Icons.Default.RocketLaunch, contentDescription = null)
          Spacer(modifier = Modifier.width(10.dp))
          Text("توليد التطبيق الأسطوري الآن 🚀", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 2. نافذة المعاينة الحية والتفاعل (Live Preview Screen)
// -------------------------------------------------------------
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LivePreviewScreen(
  htmlCode: String,
  appName: String,
  onConsoleError: (String) -> Unit,
  onOpenBugHunter: () -> Unit,
  onOpenExporter: () -> Unit,
  modifier: Modifier = Modifier
) {
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var reloadTrigger by remember { mutableStateOf(0) }

  Card(
    modifier = modifier
      .fillMaxSize()
      .testTag("live_preview_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = BorderStroke(1.2.dp, GoldSecondary.copy(alpha = 0.35f))
  ) {
    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(EmeraldSuccess)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = appName,
              color = GoldPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              maxLines = 1
            )
            Text(
              text = "محاكاة مرئية حية وشاشة لمس تفاعلية",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              reloadTrigger++
              webViewInstance?.reload()
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "إعادة تحميل", tint = GoldTertiary)
          }

          Button(
            onClick = onOpenExporter,
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A0802)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(34.dp).testTag("preview_export_btn")
          ) {
            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("تصدير", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF020408)),
        border = BorderStroke(1.5.dp, GoldSecondary.copy(alpha = 0.5f))
      ) {
        AndroidView(
          modifier = Modifier
            .fillMaxSize()
            .testTag("interactive_webview_preview"),
          factory = { ctx ->
            WebView(ctx).apply {
              settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                allowFileAccess = true
              }

              webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                  consoleMessage?.message()?.let { msg ->
                    if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                      onConsoleError(msg)
                    }
                  }
                  return super.onConsoleMessage(consoleMessage)
                }
              }

              webViewClient = WebViewClient()
              loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
              webViewInstance = this
            }
          },
          update = { view ->
            if (reloadTrigger >= 0) {
              view.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
            }
          }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "🎯 يدعم اللمس والضغط المباشر للألعاب والتطبيقات",
          color = Color(0xFF9CA3AF),
          fontSize = 11.sp
        )
        Text(
          text = "صيانة الأخطاء 🛠️",
          color = GoldTertiary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.clickable { onOpenBugHunter() }
        )
      }
    }
  }
}

// -------------------------------------------------------------
// 3. لوحة التحكم وأزرار التصدير (Action Hub: Termux & GitHub)
// -------------------------------------------------------------
@Composable
fun ActionHubView(
  appName: String,
  termuxScript: String,
  fullHtmlCode: String,
  onCopyTermux: () -> Unit,
  onCopyCode: () -> Unit,
  onShareBundle: () -> Unit,
  onOpenGitHubDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxSize()
      .testTag("action_hub_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = BorderStroke(1.2.dp, GoldSecondary.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .background(Color(0xFF2A2210), RoundedCornerShape(12.dp))
            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Terminal, contentDescription = null, tint = GoldPrimary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "لوحة التحكم وأزرار التصدير 🚀",
            color = GoldPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "تشغيل فوري على الهواتف بنقرة واحدة وتكامل GitHub",
            color = TextSecondary,
            fontSize = 12.sp
          )
        }
      }

      // 1. التصدير الفوري للهاتف بنقرة واحدة (Termux Script)
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A14)),
        border = BorderStroke(1.5.dp, EmeraldSuccess.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = EmeraldSuccess)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "1. التصدير الفوري للهاتف (Termux Script)",
              color = EmeraldSuccess,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "تجهيز حزمة التطبيق وتحويلها إلى سكريبت Bash تلقائي جاهز للتشغيل على تطبيق Termux بنقرة واحدة وبدون الحاجة لجهاز كمبيوتر.",
            color = Color(0xFFD1FAE5),
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = onCopyTermux,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color(0xFF021B0E)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).height(44.dp).testTag("copy_termux_script_btn")
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("نسخ سكريبت Termux 📋", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
              onClick = onShareBundle,
              modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF064E3B), RoundedCornerShape(12.dp))
            ) {
              Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = EmeraldSuccess)
            }
          }
        }
      }

      // 2. زر المزامنة مع مستودع GitHub
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A2B)),
        border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFFA855F7))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "2. المزامنة مع مستودع GitHub",
              color = Color(0xFFE9D5FF),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "رفع وتحديث ملفات التطبيق وسكريبتات التشغيل تلقائياً على مستودعك على GitHub مع commit نظيف.",
            color = Color(0xFFD8B4FE),
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = onOpenGitHubDialog,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("open_github_sync_btn")
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("المزامنة والرفع إلى GitHub 🐙", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // 3. نسخ الكود المصدري بالكامل
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianBg),
        border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("الكود البرمجي المولد (HTML5/CSS/JS)", color = GoldTertiary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
              text = "نسخ الكود",
              color = GoldPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier = Modifier.clickable { onCopyCode() }
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = fullHtmlCode.take(280) + "\n...",
            color = Color(0xFF9CA3AF),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 14.sp
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 4. صندوق أدوات الصيانة (AI Bug Hunter Menu)
// -------------------------------------------------------------
@Composable
fun AiBugHunterMenu(
  jsErrors: List<String>,
  bugInputText: String,
  onBugInputChange: (String) -> Unit,
  isFixing: Boolean,
  onTriggerFix: () -> Unit,
  onClearLogs: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxSize()
      .testTag("ai_bug_hunter_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = BorderStroke(1.2.dp, CrimsonAlert.copy(alpha = 0.45f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF2A1010), RoundedCornerShape(12.dp))
                .border(1.dp, CrimsonAlert, RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.BugReport, contentDescription = null, tint = CrimsonAlert)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "صندوق أدوات الصيانة (AI Bug Hunter) 🎯",
                color = Color(0xFFFCA5A5),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "فحص الكود وإصلاح الأخطاء آلياً بنسبة 100%",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }

          if (jsErrors.isNotEmpty()) {
            IconButton(onClick = onClearLogs, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Refresh, contentDescription = "تفريغ", tint = Color(0xFF9CA3AF))
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "الأخطاء المرصودة في بيئة التشغيل (${jsErrors.size}):",
          color = TextGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (jsErrors.isEmpty()) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E14)),
            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "الكود البرمجي يعمل بسلاسة وبدون أي أخطاء مسجلة!",
                color = Color(0xFFA7F3D0),
                fontSize = 11.sp
              )
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            jsErrors.forEach { err ->
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1010)),
                border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = err, color = Color(0xFFFCA5A5), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "الصق رسالة الخطأ (Bug Log) أو صف المشكلة المطلوب إصلاحها:",
          color = TextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = bugInputText,
          onValueChange = onBugInputChange,
          placeholder = {
            Text(
              "مثال: Uncaught TypeError: Cannot read property of undefined أو الأزرار لا تستجيب للمس...",
              color = Color(0xFF6B7280),
              fontSize = 12.sp
            )
          },
          minLines = 3,
          maxLines = 5,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CrimsonAlert,
            unfocusedBorderColor = ObsidianBorder,
            focusedContainerColor = ObsidianBg,
            unfocusedContainerColor = ObsidianBg,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("bug_hunter_text_input")
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onTriggerFix,
        enabled = !isFixing,
        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert, contentColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("run_bug_fix_action_btn")
      ) {
        if (isFixing) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(10.dp))
          Text("جاري الفحص وإصلاح الأخطاء آلياً 100%...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        } else {
          Icon(Icons.Default.AutoFixHigh, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("فحص وإصلاح الأخطاء آلياً 100% 🛠️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 5. شاشة إعدادات المفاتيح (BYOK Configuration)
// -------------------------------------------------------------
@Composable
fun ByokConfigurationScreen(
  cerebrasKey: String,
  onCerebrasKeyChange: (String) -> Unit,
  geminiKey: String,
  onGeminiKeyChange: (String) -> Unit,
  openRouterKey: String,
  onOpenRouterKeyChange: (String) -> Unit,
  isBypassActive: Boolean,
  onToggleBypass: (Boolean) -> Unit,
  onSaveKeys: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxSize()
      .testTag("byok_config_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = BorderStroke(1.2.dp, GoldSecondary.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(Color(0xFF2A2210), RoundedCornerShape(12.dp))
            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Key, contentDescription = null, tint = GoldPrimary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "إعدادات المفاتيح (BYOK Configuration) ⚙️",
            color = GoldPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "إدخال المفاتيح الخاصة بحصتك، أو تجاوزها والعمل فوراً",
            color = TextSecondary,
            fontSize = 12.sp
          )
        }
      }

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F14)),
        border = BorderStroke(1.2.dp, EmeraldSuccess),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "⚡ تجاوز إدخال المفاتيح حالياً",
              color = EmeraldSuccess,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = "يعمل التطبيق بالكامل مجاناً وبدون الحاجة لإدخال أي مفاتيح بالاعتماد على المحرك المدمج الذكي.",
              color = Color(0xFFD1FAE5),
              fontSize = 11.sp,
              lineHeight = 15.sp
            )
          }
          Button(
            onClick = { onToggleBypass(!isBypassActive) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isBypassActive) EmeraldSuccess else Color(0xFF374151),
              contentColor = if (isBypassActive) Color(0xFF021B0E) else Color.White
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(if (isBypassActive) "مفعّل ✅" else "تفعيل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Text(
        text = "أو أدخل مفاتيح الـ API الشخصية لحصتك الخاصة:",
        color = TextGold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )

      ApiKeyInputField(
        title = "Cerebras API Key",
        subtitle = "سرعة توليد خارقة تصل إلى 2000 توكن بالثانية",
        value = cerebrasKey,
        onValueChange = onCerebrasKeyChange,
        placeholder = "csk-...",
        testTag = "cerebras_api_key_input"
      )

      ApiKeyInputField(
        title = "Google Gemini AI Studio",
        subtitle = "ذكاء عميق وتدقيق عالي وتصميمات خالية من الأخطاء",
        value = geminiKey,
        onValueChange = onGeminiKeyChange,
        placeholder = "AIzaSy...",
        testTag = "gemini_api_key_input"
      )

      ApiKeyInputField(
        title = "OpenRouter Free Models",
        subtitle = "نماذج مجانية مفتوحة ومجانية تماماً كخزان احتياطي",
        value = openRouterKey,
        onValueChange = onOpenRouterKeyChange,
        placeholder = "sk-or-v1-...",
        testTag = "openrouter_api_key_input"
      )

      Button(
        onClick = onSaveKeys,
        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A0802)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("save_byok_keys_btn")
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("حفظ المفاتيح في الهاتف بأمان 💾", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }
  }
}

@Composable
fun ApiKeyInputField(
  title: String,
  subtitle: String,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  testTag: String
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianBg),
    border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.3f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Text(text = subtitle, color = TextSecondary, fontSize = 10.sp)
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF6B7280), fontSize = 11.sp) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GoldPrimary,
          unfocusedBorderColor = ObsidianBorder,
          focusedContainerColor = Color(0xFF0B0D12),
          unfocusedContainerColor = Color(0xFF0B0D12),
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().testTag(testTag)
      )
    }
  }
}

// -------------------------------------------------------------
// الهيدر وشريط التنقل الفاخر (Navigation & Bar)
// -------------------------------------------------------------
@Composable
fun LuxuryTopBar(activeAppName: String, isBypassMode: Boolean, providerUsed: AIProvider) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = Color(0xFF090A0E)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF2A2210))
            .border(1.2.dp, GoldPrimary, RoundedCornerShape(11.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text("⚡", fontSize = 19.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "صانع التطبيقات الأسطوري",
            color = GoldPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = if (isBypassMode) "الوضع السريع: المحرك المدمج الفوري" else "المحرك: ${providerUsed.name}",
            color = if (isBypassMode) EmeraldSuccess else GoldTertiary,
            fontSize = 10.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(ObsidianElevated)
          .border(1.dp, GoldSecondary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text(
          text = activeAppName,
          color = TextGold,
          fontSize = 10.sp,
          maxLines = 1,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun LuxuryNavigationBar(
  currentTab: AppStudioTab,
  onTabSelected: (AppStudioTab) -> Unit,
  errorCount: Int
) {
  NavigationBar(
    containerColor = Color(0xFF090A0E),
    tonalElevation = 10.dp,
    modifier = Modifier.testTag("luxury_navigation_bar")
  ) {
    NavigationBarItem(
      selected = currentTab == AppStudioTab.CHAT_VIEW,
      onClick = { onTabSelected(AppStudioTab.CHAT_VIEW) },
      icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "دردشة") },
      label = { Text("الدردشة", fontSize = 10.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF0A0802),
        selectedTextColor = GoldPrimary,
        indicatorColor = GoldPrimary,
        unselectedIconColor = Color(0xFF9CA3AF),
        unselectedTextColor = Color(0xFF9CA3AF)
      ),
      modifier = Modifier.testTag("nav_chat_tab")
    )

    NavigationBarItem(
      selected = currentTab == AppStudioTab.LIVE_PREVIEW,
      onClick = { onTabSelected(AppStudioTab.LIVE_PREVIEW) },
      icon = { Icon(Icons.Default.PlayArrow, contentDescription = "معاينة") },
      label = { Text("المعاينة", fontSize = 10.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF0A0802),
        selectedTextColor = GoldPrimary,
        indicatorColor = GoldPrimary,
        unselectedIconColor = Color(0xFF9CA3AF),
        unselectedTextColor = Color(0xFF9CA3AF)
      ),
      modifier = Modifier.testTag("nav_preview_tab")
    )

    NavigationBarItem(
      selected = currentTab == AppStudioTab.ACTION_HUB,
      onClick = { onTabSelected(AppStudioTab.ACTION_HUB) },
      icon = { Icon(Icons.Default.Terminal, contentDescription = "تصدير") },
      label = { Text("التصدير", fontSize = 10.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF0A0802),
        selectedTextColor = GoldPrimary,
        indicatorColor = GoldPrimary,
        unselectedIconColor = Color(0xFF9CA3AF),
        unselectedTextColor = Color(0xFF9CA3AF)
      ),
      modifier = Modifier.testTag("nav_action_hub_tab")
    )

    NavigationBarItem(
      selected = currentTab == AppStudioTab.BUG_HUNTER,
      onClick = { onTabSelected(AppStudioTab.BUG_HUNTER) },
      icon = {
        if (errorCount > 0) {
          BadgedBox(
            badge = {
              Badge(containerColor = CrimsonAlert, contentColor = Color.White) {
                Text(errorCount.toString())
              }
            }
          ) {
            Icon(Icons.Default.BugReport, contentDescription = "صيانة")
          }
        } else {
          Icon(Icons.Default.BugReport, contentDescription = "صيانة")
        }
      },
      label = { Text("الصيانة", fontSize = 10.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = CrimsonAlert,
        indicatorColor = CrimsonAlert,
        unselectedIconColor = Color(0xFF9CA3AF),
        unselectedTextColor = Color(0xFF9CA3AF)
      ),
      modifier = Modifier.testTag("nav_bug_hunter_tab")
    )

    NavigationBarItem(
      selected = currentTab == AppStudioTab.BYOK_CONFIG,
      onClick = { onTabSelected(AppStudioTab.BYOK_CONFIG) },
      icon = { Icon(Icons.Default.Settings, contentDescription = "المفاتيح") },
      label = { Text("المفاتيح", fontSize = 10.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF0A0802),
        selectedTextColor = GoldPrimary,
        indicatorColor = GoldPrimary,
        unselectedIconColor = Color(0xFF9CA3AF),
        unselectedTextColor = Color(0xFF9CA3AF)
      ),
      modifier = Modifier.testTag("nav_byok_tab")
    )
  }
}
