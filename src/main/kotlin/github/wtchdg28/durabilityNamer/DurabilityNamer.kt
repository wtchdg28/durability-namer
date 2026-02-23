package github.wtchdg28.durabilityNamer

import org.bukkit.plugin.java.JavaPlugin

class DurabilityNamer : JavaPlugin() {

    override fun onEnable() {
        // Register the listener to handle join events
        server.pluginManager.registerEvents(JoinListener(), this)

        logger.info("DurabilityNamer enabled.")
    }

    override fun onDisable() {
        logger.info("DurabilityNamer disabled.")
    }
}