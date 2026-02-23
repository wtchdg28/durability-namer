package github.wtchdg28.durabilityNamer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class JoinListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.inventory.contents.filterNotNull().forEach { updateItemName(it) }
    }

    @EventHandler
    fun onItemDamage(event: PlayerItemDamageEvent) {
        updateItemName(event.item)
    }

    private fun updateItemName(item: ItemStack) {
        val meta = item.itemMeta ?: return

        if (meta is Damageable && item.type.maxDurability > 0) {

            // 1. Get the base name (Translation or Anvil Name)
            val baseComponent = if (meta.hasDisplayName()) {
                // Get current display name and remove children (previous tags)
                // This keeps the root (Translation-Key or Anvil-Text) intact!
                meta.displayName()!!.children(emptyList())
            } else {
                // No custom name? Use the translation key (Client-side translation)
                Component.translatable(item.type.translationKey())
            }

            // 2. Calculate values
            val max = item.type.maxDurability.toInt()
            val current = max - meta.damage

            val color = when {
                current < max * 0.1 -> NamedTextColor.RED
                current < max * 0.5 -> NamedTextColor.YELLOW
                else -> NamedTextColor.GRAY
            }

            // 3. Create the tag as a child component
            val tag = Component.text(" ($current/$max)")
                .color(color)
                .decoration(TextDecoration.ITALIC, false)

            // 4. Build final name
            // We append the tag directly to the base component (root)
            // We also force italic to false to prevent the slanted vanilla look
            val finalName = baseComponent
                .decoration(TextDecoration.ITALIC, false)
                .append(tag)

            meta.displayName(finalName)
            item.itemMeta = meta
        }
    }
}