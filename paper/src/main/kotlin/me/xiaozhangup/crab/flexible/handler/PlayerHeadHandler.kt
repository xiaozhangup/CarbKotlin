package me.xiaozhangup.crab.flexible.handler

import me.xiaozhangup.crab.flexible.FlexibleItemHandler
import com.google.gson.JsonParser
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.net.URI
import java.util.*

internal object PlayerHeadHandler : FlexibleItemHandler {

    override fun getNamespace(): String = "head"

    override fun getStack(string: String): Optional<ItemStack> {
        val item = ItemStack(Material.PLAYER_HEAD)
        item.editMeta { meta ->
            meta as SkullMeta
            if (string.length > 20) {
                val texture = JsonParser.parseString(String(Base64.getDecoder().decode(string), Charsets.UTF_8))
                    .asJsonObject.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").asString
                val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(string.toByteArray()), "WhaleSkull")
                profile.setTextures(profile.textures.apply { skin = URI(texture).toURL() })
                meta.playerProfile = profile
            } else {
                @Suppress("DEPRECATION")
                meta.owner = string
            }
        }
        return Optional.of(item)
    }

    override fun getName(item: ItemStack): Optional<String> {
        if (item.type != Material.PLAYER_HEAD) return Optional.empty()
        val meta = item.itemMeta as SkullMeta
        return Optional.ofNullable(meta.playerProfile?.properties?.firstOrNull { it.name == "textures" }?.value)
    }
}
