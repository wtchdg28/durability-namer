package github.wtchdg28.durabilityNamer

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class JoinListener : Listener {

    private val mm = MiniMessage.miniMessage()
    private val plainSerializer = PlainTextComponentSerializer.plainText()

    // Pattern to catch the durability tag at the end
    private val durabilityPattern = Regex("""\s*[\(\[].*?\d+/\d+.*?[\)\]]$""")

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
            // 1. Get current display name component
            val currentNameComponent = item.displayName()

            // 2. Serialize and clean it
            val rawName = plainSerializer.serialize(currentNameComponent)
            var cleanName = rawName.replace(durabilityPattern, "")
                .replace("[", "").replace("]", "")
                .trim()

            // FALLBACK: If the name became empty, use the localized material name
            if (cleanName.isEmpty()) {
                // This gets the readable name like "Diamond Pickaxe" from the material
                cleanName = item.type.name.replace("_", " ").lowercase()
                    .split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            }

            val max = item.type.maxDurability.toInt()
            val current = max - meta.damage

            val color = when {
                current < max * 0.1 -> "<red>"
                current < max * 0.5 -> "<yellow>"
                else -> "<gray>"
            }

            // 3. Build the final name
            // <reset> ensures no weird formatting leaks
            // <italic:false> stops the annoying slant
            val finalName = mm.deserialize("<reset><white><italic:false>$cleanName</italic:false></white> $color($current/$max)")
                .decoration(TextDecoration.ITALIC, false)

            meta.displayName(finalName)
            item.itemMeta = meta
        }
    }
}