package af.shizuku.manager.update

import af.shizuku.manager.ShizukuSettings
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AppUpdatePolicyTest : FunSpec({
    test("both upstream release channels return disabled instead of checking for a release") {
        for (channel in listOf("stable", "dev")) {
            UpdateChecker.checkForUpdate(channel) shouldBe UpdateChecker.CheckResult.Disabled
        }
    }

    test("automatic checking and installation stay disabled without consulting stored preferences") {
        // No Android preferences are initialized in this JVM test. A preference-backed getter
        // would fail here, so this also protects the fixed policy against backup/import state.
        ShizukuSettings.isAutoUpdateEnabled() shouldBe false
        ShizukuSettings.isAutoInstallEnabled() shouldBe false
    }

    test("startup release notes are not fetched for this custom distribution") {
        UpdateChecker.fetchReleaseNotesForTag("v13.6.0.r2551") shouldBe null
    }
})
