# 保留与 Compose 相关的类不被混淆
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }

-dontwarn com.install4j.**
-keep class com.install4j.**

# 其他的混淆规则
-dontwarn androidx.compose.**
-dontwarn org.jetbrains.compose.**

# 进一步优化混淆和压缩
-optimizationpasses 5
