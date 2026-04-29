package com.fish.extendedae_plus_client.render.widgets.button

import appeng.client.gui.Icon
import appeng.client.gui.style.Blitter
import com.fish.extendedae_plus_client.ExtendedAEPlusClient
import net.minecraft.resources.ResourceLocation

enum class EAEPIcon(val x: Int, val Y: Int, val width: Int = 16, val height: Int = 16) : IButtonIcon {
 SAVE_CENTER(0, 0),
 SAVE_UP(16, 0),
 SAVE_DOWN(32, 0),
 ;

 override val blitter: Blitter
 get() = Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT)
 .src(x, Y, width, height)

 override val aeIcon: Icon
 get() = when (this) {
 SAVE_CENTER -> Icon.SEARCH_START
 SAVE_UP -> Icon.PLUS
 SAVE_DOWN -> Icon.MINUS
 }

 @JvmRecord
 private data class AEIcon(override val aeIcon: Icon) : IButtonIcon {
 override val blitter: Blitter
 get() = aeIcon.blitter
 }

 companion object {
 val TEXTURE: ResourceLocation = ExtendedAEPlusClient.getLocation("textures/gui/icons.png")
 const val TEXTURE_WIDTH: Int = 64
 const val TEXTURE_HEIGHT: Int = 64

 @JvmStatic
 fun fromAEIcon(aeIcon: Icon): IButtonIcon {
 return AEIcon(aeIcon)
 }
 }
}