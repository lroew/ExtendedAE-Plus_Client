package com.fish.extendedae_plus_client.impl.event

import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.client.gui.AEBaseScreen
import appeng.core.AEConfig
import appeng.helpers.InventoryAction
import appeng.menu.me.common.MEStorageMenu
import com.fish.extendedae_plus_client.ExtendedAEPlusClient
import com.fish.extendedae_plus_client.config.EAEPCKeyMapping
import com.fish.extendedae_plus_client.integration.impl.recipeViewer.HelperRecipeViewer
import com.fish.extendedae_plus_client.mixin.impl.helper.HelperSearchField
import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.Slot
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.client.event.ScreenEvent
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = ExtendedAEPlusClient.MODID, value = [Dist.CLIENT])
object EventScreenActions {
 private var isPulled = false

 @SubscribeEvent
 fun onMouseButtonPre(event: InputEvent.MouseButton.Pre) {
 if (Minecraft.getInstance().player == null) return
 if (Minecraft.getInstance().screen == null) return

 val menu = Minecraft.getInstance().player!!.containerMenu
 if (menu !is MEStorageMenu) return

 if (HelperRecipeViewer.isCheatMode()) return

 if (event.action != GLFW.GLFW_PRESS) {
 if (isPulled) event.setCanceled(true)
 isPulled = false
 return
 }

 val infoStack = findHoveredStack(menu) ?: return

 val pulled = HelperRecipeViewer.matchesKey(event.button)
 if (pulled != null) {
 menu.handleInteraction(
 infoStack.second, getAction(infoStack, pulled)
 )
 isPulled = true
 return
 }

 if (event.button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
 menu.handleInteraction(infoStack.second, InventoryAction.AUTO_CRAFT)
 event.setCanceled(true)
 }
 }

 @SubscribeEvent
 fun onKeyPressedPre(event: ScreenEvent.KeyPressed.Pre) {
 if (Minecraft.getInstance().player == null) return

 val keyMapping = EAEPCKeyMapping.fillToSearchField.get()
 if (keyMapping.matches(event.keyCode, event.scanCode)) {
 var stack: GenericStack? = null
 val stacks = HelperRecipeViewer.hoveredStacks()
 if (!stacks.isEmpty()) stack = stacks[0]
 if (stack == null && Minecraft.getInstance().screen is AEBaseScreen<*>) {
 val screen = Minecraft.getInstance().screen as AEBaseScreen<*>
 val slot: Slot = screen.slotUnderMouse ?: return
 stack = GenericStack.fromItemStack(slot.item)
 }
 if (stack == null) return
 val name = stack.what().getDisplayName().string

 if (AEConfig.instance().isUseExternalSearch) {
 HelperRecipeViewer.setSearchText(name)
 } else if (Minecraft.getInstance().screen is HelperSearchField) {
 val screen = Minecraft.getInstance().screen as HelperSearchField
 screen.getSearchField().value = name
 screen.`eaep$setSearchText`(name)
 }
 event.setCanceled(true)
 return
 }

 val clearSearchKey = EAEPCKeyMapping.clearSearch.get()
 if (clearSearchKey.matches(event.keyCode, event.scanCode)) {
 if (Minecraft.getInstance().screen is HelperSearchField) {
 val screen = Minecraft.getInstance().screen as HelperSearchField
 screen.getSearchField().value = ""
 screen.`eaep$setSearchText`("")
 event.setCanceled(true)
 }
 return
 }
 }

 private fun findHoveredStack(menu: MEStorageMenu): Pair<AEKey, Long>? {
 if (menu.clientRepo == null) return null

 val stacks = HelperRecipeViewer.hoveredStacks()
 var stack: GenericStack? = if (stacks.isEmpty()) null else stacks[0]
 if (stack == null) return null

 for (entry in menu.clientRepo!!.allEntries) {
 if (stack!!.what() != entry.what) {
 if (stack.what() !is AEItemKey) continue

 val unwrapped = GenericStack.unwrapItemStack((stack.what as AEItemKey).toStack())
 if (unwrapped == null || unwrapped.what() != entry.what) continue
 stack = unwrapped
 }

 return Pair<AEKey, Long>(stack.what(), entry.serial)
 }
 return null
 }

 private fun getAction(
 infoStack: Pair<AEKey, Long>,
 pulled: Pair<Boolean, Boolean>
 ): InventoryAction {
 return if (infoStack.first is AEItemKey) {
 if (pulled.first && pulled.second) InventoryAction.SHIFT_CLICK
 else if (pulled.first) InventoryAction.PICKUP_OR_SET_DOWN
 else if (pulled.second) // 这里没有对应的 action
 InventoryAction.SHIFT_CLICK
 else InventoryAction.PICKUP_SINGLE
 } else {
 if (pulled.first && pulled.second) InventoryAction.FILL_ENTIRE_ITEM_MOVE_TO_PLAYER
 else if (pulled.first) InventoryAction.FILL_ENTIRE_ITEM
 else if (pulled.second)
 InventoryAction.FILL_ITEM_MOVE_TO_PLAYER
 else InventoryAction.FILL_ITEM
 }
 }
}