package com.fish.extendedae_plus_client.config

import com.fish.extendedae_plus_client.ExtendedAEPlusClient
import com.fish.extendedae_plus_client.util.UtilKeyBuilder
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.jarjar.nio.util.Lazy
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.settings.IKeyConflictContext
import net.neoforged.neoforge.client.settings.KeyConflictContext
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = ExtendedAEPlusClient.MODID)
object EAEPCKeyMapping {
 private val mappings: MutableSet<Lazy<KeyMapping>> = HashSet<Lazy<KeyMapping>>()

 private val CATEGORY: String = UtilKeyBuilder.of(UtilKeyBuilder.keyCategory).buildRaw()

 @JvmField
 val fillToSearchField: Lazy<KeyMapping> = this.register(
 "fill_to_search_field",
 KeyConflictContext.GUI,
 GLFW.GLFW_KEY_F
 )

 @JvmField
 val openProviderList: Lazy<KeyMapping> = this.register(
 "open_provider_list",
 KeyConflictContext.GUI,
 GLFW.GLFW_KEY_G
 )

 @JvmField
 val markAutoComplete: Lazy<KeyMapping> = this.register(
 "mark_auto_complete",
 KeyConflictContext.GUI,
 GLFW.GLFW_KEY_H
 )

 @JvmField
 val clearSearch: Lazy<KeyMapping> = this.register(
 "clear_search",
 KeyConflictContext.GUI,
 GLFW.GLFW_KEY_ESCAPE
 )

 private fun register(
 name: String,
 keyConflictContext: IKeyConflictContext,
 inputType: InputConstants.Type,
 keyCode: Int,
 category: String
 ): Lazy<KeyMapping> {
 val mapping = Lazy.of<KeyMapping> {
 KeyMapping(
 UtilKeyBuilder.of(UtilKeyBuilder.key)
 .addStr(name)
 .buildRaw(),
 keyConflictContext,
 inputType,
 keyCode,
 category
 )
 }
 mappings.add(mapping)
 return mapping
 }

 private fun register(
 name: String, keyConflictContext: IKeyConflictContext, keyCode: Int
 ) = register(name, keyConflictContext, InputConstants.Type.KEYSYM, keyCode, CATEGORY)

 @SubscribeEvent
 private fun onKeyMappingReg(event: RegisterKeyMappingsEvent) {
 this.mappings
 .map(Lazy<KeyMapping>::get)
 .forEach(event::register)
 }
}