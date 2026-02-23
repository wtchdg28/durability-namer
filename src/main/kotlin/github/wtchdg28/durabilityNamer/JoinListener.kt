package github.wtchdg28.durabilityNamer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class JoinListener : Listener {

    private val mm = MiniMessage.miniMessage()
    private val plainSerializer = PlainTextComponentSerializer.plainText()

    // Pattern to match " (123/456)" at the end of a string
    private val durabilityPattern = Regex("""\s\(\d+/\d+\)$""")

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val inventory = event.player.inventory

        // Iterate through all non-empty slots
        inventory.contents.filterNotNull().forEach { item ->
            updateItemName(item)
        }
    }

    private fun updateItemName(item: ItemStack) {
        val meta = item.itemMeta ?: return

        // Check if item is a tool/armor with durability
        if (meta is Damageable && item.type.maxDurability > 0) {

            // Get current name as plain text and remove existing durability tags
            val currentDisplayName = item.displayName()
            val rawName = plainSerializer.serialize(currentDisplayName)
            val cleanName = rawName.replace(durabilityPattern, "").trim()

            // Calculate current durability
            val max = item.type.maxDurability.toInt()
            val current = max - meta.damage

            // Pick color based on remaining percentage
            val color = when {
                current < max * 0.1 -> "<red>"
                current < max * 0.5 -> "<yellow>"
                else -> "<gray>"
            }

            // Build new component with MiniMessage
            val durabilityTag = " $color($current/$max)"
            val finalName = Component.text(cleanName)
                .append(mm.deserialize(durabilityTag))

            // Apply meta changes back to the item
            meta.displayName(finalName)
            item.itemMeta = meta
        }
    }
}