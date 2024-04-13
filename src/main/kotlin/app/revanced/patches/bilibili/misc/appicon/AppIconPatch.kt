package app.revanced.patches.bilibili.misc.appicon

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Patch(
    name = "App icon",
    description = "恢复APP图标",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object AppIconPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        val iconFiles = arrayOf(
            "ic_launcher.png",
            "ic_launcher_background.png",
            "ic_launcher_foreground.png",
            "ic_launcher_foreground_round.png",
            "ic_launcher_round.png",
            "ic_launcher_monochrome.png"
        )
        val iconPaths = arrayOf(
            "mipmap-mdpi",
            "mipmap-hdpi",
            "mipmap-xhdpi",
            "mipmap-xxhdpi",
            "mipmap-xxxhdpi"
        )
        iconPaths.forEach {
            context.copyResources("bilibili/appicon", ResourceGroup(it, *iconFiles))
        }
        ResourceGroup(
            "mipmap-anydpi-v26",
            "ic_launcher.xml", "ic_launcher_round.xml"
        ).let {
            context.copyResources("bilibili/appicon", it)
        }
    }
}
