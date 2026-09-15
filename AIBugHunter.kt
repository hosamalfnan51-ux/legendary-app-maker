package com.example.appbuilder.data.repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BugFixResult(
    val isFixed: Boolean,
    val correctedCode: String?,
    val explanation: String?,
    val apiUsed: String
)

class AIBugHunter(private val aiRouter: ResilientAIRouter) {
    // الدالة الرئيسية لاستقبل الكود المعطوب ورسالة الخطأ وإصلاحها تلقائياً
    suspend fun analyzeAndFixBug(brokenCode: String, errorMessage: String): BugFixResult = withContext(Dispatchers.IO) {
        val engineeringPrompt = """
            أنت مهندس صيانة برمجيات محترف. لديك كود برميجي يحتوي على أخطاء، ورسالة الخطأ الناتجة عن التشغيل.
            مهمتك هي اكتشاف الثغرة، وإصلاحها، وإعادة الكود كاملاً وسليماً 100% بدون اختصارات.
            الكود المعطوب: $brokenCode
            رسالة الخطأ: $errorMessage
            أخرج الرد بصيغة واضحة: الكود المصحح أولاً داخل صندوق برمجي، يليه شرح بسيط باللغة العربية للسبب والحل.
        """.trimIndent()
        val routerResult = aiRouter.generateApplicationCode(engineeringPrompt)
        if (routerResult.isSuccess && routerResult.generatedCode != null) {
            return@withContext BugFixResult(true, routerResult.generatedCode, "تم اكتشاف الخطأ وإصلاحه بنجاح.", routerResult.providerUsed.name)
        } else {
            return@withContext BugFixResult(false, null, "فشلت محاولة الإصلاح: ${routerResult.errorMessage}", "NONE")
        }
    }
}

