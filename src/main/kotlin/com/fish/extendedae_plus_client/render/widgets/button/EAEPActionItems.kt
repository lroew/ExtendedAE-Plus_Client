package com.fish.extendedae_plus_client.render.widgets.button

import appeng.client.gui.Icon
import appeng.client.gui.style.Blitter
import appeng.client.gui.Icon
import com.fish.extendedae_plus_client.render.widgets.button.EAEPIcon.Companion.fromAEIcon
import com.fish.extendedae_plus_client.util.UtilKeyBuilder
import net.minecraft.network.chat.Component

enum class EAEPActionItems(
 val icon: IButtonIcon,
 @JvmField val actionName: Component,
 @JvmField val tooltip: Component?,
 val group: String
) {
 BACKING_OUT(fromAEIcon(Icon.INVALID), Component.empty(), Component.empty(), ""),

 ALIAS_ADD(EAEPIcon.SAVE_UP, Component.translatable("key.extendedae_plus_client.alias_add"), Component.translatable("tooltip.extendedae_plus_client.alias_add"), "provider_list"),
 ALIAS_REMOVE(EAEPIcon.SAVE_DOWN, Component.translatable("key.extendedae_plus_client.alias_remove"), Component.translatable("tooltip.extendedae_plus_client.alias_remove"), "provider_list"),

 SELECT_ALL(fromAEIcon(Icon.CHECKBOX), Component.translatable("key.extendedae_plus_client.select_all"), Component.translatable("tooltip.extendedae_plus_client.select_all"), "provider_list"),
 DESELECT_ALL(fromAEIcon(Icon.CHECKBOX_EMPTY), Component.translatable("key.extendedae_plus_client.deselect_all"), Component.translatable("tooltip.extendedae_plus_client.deselect_all"), "provider_list"),

 ;

 constructor(icon: IButtonIcon, actionGroup: String) :
 this(icon, Component.empty(), Component.empty(), actionGroup)

 constructor(icon: IButtonIcon, actionGroup: String, additionalKey: String) : this(
 icon,
 UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip)
 .addStr(actionGroup)
 .build(),
 UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip)
 .addStr(actionGroup)
 .addStr(additionalKey)
 .build(),
 actionGroup
 )

 val iconBlitter: Blitter
 get() = icon.blitter
 val aeIcon: Icon
 get() = icon.aeIcon

 fun hasName(): Boolean {
 return !actionName.string.isEmpty()
 }

 companion object {
 val GROUPED_ACTIONS: MutableMap<String, MutableList<EAEPActionItems>> = HashMap()

 init {
 for (action in entries) {
 if (!action.group.isEmpty()) GROUPED_ACTIONS.computeIfAbsent(
 action.group
 ) { _ -> ArrayList() }.add(action)
 }
 }
 }
}