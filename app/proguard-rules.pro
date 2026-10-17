-keep class com.soreverse.mcp.core.BackupCrypto {
    *;
}

-keep class com.lambdapioneer.argon2kt.** {
    *;
}

-keep class com.soreverse.mcp.nativecore.RizinNativeEngine {
    *;
}

-keep class com.soreverse.mcp.engine.LiefEngine {
    *;
}

-keep class com.soreverse.mcp.blutter.** {
    *;
}

-keep class com.github.unidbg.** {
    *;
}

# scijava-native-lib-loader is transitively pulled in by zhkl0228:unicorn:1.0.15.
# Unicorn2Factory's static init block calls NativeLoader.loadLibrary("unicorn"),
# and the call site only catches IOException. If R8 strips NativeLoader, the
# class load fails with NoClassDefFoundError (a LinkageError, NOT an IOException)
# and Unicorn2Factory can never be initialized — Unidbg degrades completely.
# Keep all scijava.nativelib classes so NativeLoader remains on the classpath.
-keep class org.scijava.nativelib.** {
    *;
}

-keep class unicorn.** {
    *;
}

-keep class net.fornwall.jelf.** {
    *;
}

-keep class capstone.** {
    *;
}

-keep class unicorn.** {
    *;
}

-keep class com.sun.jna.** {
    *;
}

-keep class com.sun.jna.ptr.** {
    *;
}

-keep class com.sun.jna.win32.** {
    *;
}

-keep class net.dongliu.apk.parser.** {
    *;
}

-keep class com.lambdapioneer.argon2kt.** {
    *;
}

-dontwarn com.github.unidbg.**
-dontwarn unicorn.**
-dontwarn net.fornwall.jelf.**
-dontwarn capstone.**
-dontwarn com.sun.jna.**
-dontwarn net.dongliu.apk.parser.**
-dontwarn com.google.common.collect.ArrayListMultimap
-dontwarn com.google.common.collect.Multimap
-dontwarn java.awt.Color
-dontwarn java.awt.Font
-dontwarn java.awt.Point
-dontwarn java.awt.Rectangle
-dontwarn javax.money.CurrencyUnit
-dontwarn javax.money.Monetary
-dontwarn org.javamoney.moneta.Money
-dontwarn org.joda.time.**
-dontwarn springfox.documentation.spring.web.json.Json

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

-keep class kotlinx.coroutines.flow.Flow { *; }
-keep class kotlin.reflect.jvm.internal.LazyKProperty { *; }
-keepclassmembers class ** {
    static kotlin.reflect.KProperty[] $$delegatedProperties;
}

-dontwarn java.lang.management.**
-dontwarn org.slf4j.**

# The AGPL-3.0 license text must reach the shipped classes.dex. R8 removes
# unreferenced constants, and Provenance.LICENSE_TEXT has no runtime caller that
# reads it, so keep the object wholesale - name and all members - rather than
# betting on one field surviving.
-keep class com.soreverse.mcp.core.Provenance {
    *;
}

# The integrity guards must survive minification as addressable classes. With
# no keep rule R8 is free to horizontally merge an object whose members are
# all static into its call sites, which removes the class from both the dex
# and mapping.txt - observed on real CI: IntegrityGuard absent from the R8
# mapping while AppListGuard / SelfArtifactGuard (same package, same config)
# were present, failing the guard-class assertion in the VMP workflow's
# mapprefix.py. The VMP step derives --true-vmp-prefix for these classes from
# mapping.txt, so a merged-away guard silently drops out of virtualization.
# Same precedent as Provenance above: keep the objects wholesale.
-keep class com.soreverse.mcp.core.IntegrityGuard {
    *;
}
-keep class com.soreverse.mcp.core.AppListGuard {
    *;
}
-keep class com.soreverse.mcp.core.SelfArtifactGuard {
    *;
}

# ---------------------------------------------------------------------------
# APK full-chain toolchain (jadx / ARSCLib+APKEditor / smali / apksig /
# bouncycastle / DexKit). All of these are reflection- and service-loader-heavy:
# jadx resolves plugins and code writers by name, APKEditor/ARSCLib discover
# handlers through reflection, and apksig reaches Bouncy Castle providers via
# Security.getProvider at runtime. R8 cannot see those edges.
# ---------------------------------------------------------------------------
-keep class jadx.** { *; }
-keep class jadx.api.** { *; }
-dontwarn jadx.**

-keep class com.reandroid.** { *; }
-keep class io.github.reandroid.** { *; }
-dontwarn com.reandroid.**
-dontwarn io.github.reandroid.**

-keep class com.android.tools.smali.** { *; }
-dontwarn com.android.tools.smali.**

-keep class com.android.apksig.** { *; }
-dontwarn com.android.apksig.**

-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# DexKit is a C++/JNI library: its Java entry points and query DSL classes are
# addressed from native code and by reflection from the query builder.
-keep class org.luckypray.dexkit.** { *; }
-dontwarn org.luckypray.dexkit.**
