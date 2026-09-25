@file:Suppress("unused")

package me.xiaozhangup.carb.util

import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.without
import plutoproject.adventurekt.text.style.italic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.xiaozhangup.carb.flexible.flexibleItem
import me.xiaozhangup.carb.util.ItemStackBuilder.Companion.getTextureURL
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt
import me.xiaozhangup.carb.common.util.componentList
import java.net.URL
import java.util.*

/**
 * 基于 Whale 的 Adventure 物品构建工具，字符串名称与描述使用 MiniMessage。
 */
class ItemStackBuilder(itemStack: ItemStack) {
    private var sourceItem = itemStack.clone() // 所有操作都基于此物品
    private var amount = itemStack.amount // 数量
    private var flags = itemStack.itemFlags.toMutableSet() // 物品标记
    private var lore: MutableList<Component> = itemStack.lore()?.toMutableList() ?: mutableListOf() // Lore
    private val itemMeta: ItemMeta = itemStack.itemMeta // 仅在初始化时使用
    private var enchantments: MutableMap<Enchantment, Int> = // 附魔
        if (itemStack.hasItemMeta() && itemMeta is EnchantmentStorageMeta) {
            itemMeta.storedEnchants
        } else {
            itemStack.enchantments
        }.toMutableMap()
    private var customName: Component? = // 名称
        if (itemStack.hasItemMeta() && itemMeta.hasCustomName()) {
            itemMeta.customName()
        } else null
    private var itemName: Component? =
        if (itemStack.hasItemMeta() && itemMeta.hasItemName()) {
            itemMeta.itemName()
        } else null
    private var color: Color? = if (itemStack.hasItemMeta()) {
        itemMeta.let { meta ->
            when (meta) {
                is LeatherArmorMeta -> meta.color
                is PotionMeta -> meta.color
                else -> null
            }
        }
    } else null
    private var material: Material = itemStack.type // 类型
    private var callback: MutableList<ItemMeta.() -> Unit> = mutableListOf() // 自定义元数据操作

    fun build(): ItemStack {
        val item = if (material == sourceItem.type) sourceItem else ItemStack(material)

        item.editMeta { meta ->
            // 数量 (允许更多)
            if (item.maxStackSize < amount) {
                meta.setMaxStackSize(amount)
            }
            item.amount = amount

            // 基础内容
            meta.displayName(customName)
            meta.itemName(itemName)
            if (lore.isNotEmpty()) meta.lore(lore)
            else meta.lore(null)

            // 刷新物品 Flags
            meta.removeItemFlags(*meta.itemFlags.toTypedArray())
            meta.addItemFlags(*flags.toTypedArray())

            // 附魔操作
            if (meta is EnchantmentStorageMeta) {
                enchantments.forEach { (e, lvl) -> meta.addStoredEnchant(e, lvl, true) }
            } else {
                enchantments.forEach { (e, lvl) -> meta.addEnchant(e, lvl, true) }
            }

            // 颜色操作
            when (meta) {
                is LeatherArmorMeta -> {
                    meta.setColor(color)
                }

                is PotionMeta -> {
                    meta.color = color
                }
            }

            // 自定义的操作内容
            callback.forEach { it.invoke(meta) }
        }
        return item
    }

    fun material() = material

    /**
     * 如果你更改材质, 那么只有如下数据会保留
     * Name, Lore, Amount, Flags, Enchantments
     */
    fun material(material: Material) {
        this.material = material
    }

    fun lore() = lore

    fun lore(vararg lore: Component) {
        this.lore = lore.toMutableList()
    }

    fun lore(vararg lore: String) {
        this.lore = lore.map {
            component { mini(it) without italic }
        }.toMutableList()
    }

    fun lore(lore: List<Component>) {
        this.lore = lore.toMutableList()
    }

    fun lore(lore: ComponentKt.() -> Unit) {
        this.lore = componentList { lore() }
    }

    /**
     * 获取客户端测物品名 (玩家看到的)
     */
    fun clientName() = customName ?: itemName ?: Component.translatable(material.translationKey())

    fun name() = customName

    fun name(name: Component) {
        this.customName = name
    }

    fun name(name: String) {
        name { mini(name) without italic }
    }

    fun name(component: ComponentKt.() -> Unit) {
        this.customName = component { component() }
    }

    fun amount() = amount

    fun amount(amount: Int) {
        this.amount = amount
    }

    fun flags() = flags

    fun flags(vararg flags: ItemFlag) {
        this.flags = flags.toMutableSet()
    }

    fun enchantments() = enchantments

    fun enchantments(vararg enchantments: Pair<Enchantment, Int>) {
        this.enchantments = enchantments.toMap().toMutableMap()
    }

    /**
     * 设置物品颜色
     */
    fun color(color: Color?) {
        this.color = color
    }

    /**
     * 使物品不可破坏
     */
    fun unbreakable(unbreakable: Boolean) {
        meta {
            isUnbreakable = unbreakable
        }
    }

    /**
     * 隐藏所有信息
     */
    fun hideAll() {
        flags.addAll(ItemFlag.entries)
    }

    /**
     * 隐藏悬浮提示
     */
    fun hideTooltip() {
        meta {
            isHideTooltip = true
        }
    }

    /**
     * 使物品发光
     */
    fun shiny() {
        flags += ItemFlag.HIDE_ENCHANTS
        enchantments[Enchantment.LURE] = 1
    }

    fun meta(block: ItemMeta.() -> Unit) {
        this.callback += block
    }

    companion object {
        private val JSON = Json { ignoreUnknownKeys = true }

        fun getTextureURL(headBase64: String): URL {
            val jsonString = Base64.getDecoder().decode(headBase64)
                .toString(Charsets.UTF_8)
            val root: JsonElement = JSON.parseToJsonElement(jsonString)
            val url = root.jsonObject
                .getValue("textures")
                .jsonObject
                .getValue("SKIN")
                .jsonObject
                .getValue("url")
                .jsonPrimitive
                .content

            return URL(url)
        }

        fun generateSkinBase64(textureUrl: String): String {
            val json = """{"textures":{"SKIN":{"url":"$textureUrl"}}}"""
            return Base64.getEncoder().encodeToString(json.toByteArray())
        }
    }
}

fun itemStack(itemStack: ItemStack, block: ItemStackBuilder.() -> Unit = {}): ItemStack {
    val builder = ItemStackBuilder(itemStack)
    block(builder)
    return builder.build()
}

fun itemStack(material: Material, block: ItemStackBuilder.() -> Unit = {}): ItemStack {
    val builder = ItemStackBuilder(ItemStack(material))
    block(builder)
    return builder.build()
}

fun itemStack(flexibleItem: String, block: ItemStackBuilder.() -> Unit = {}): ItemStack {
    val itemStack = flexibleItem(flexibleItem)
        ?: throw IllegalArgumentException("Cannot parse flexible item: $flexibleItem")
    return itemStack(itemStack, block)
}

@Suppress("DEPRECATION")
fun skull(texture: String, block: (ItemStackBuilder.() -> Unit)? = null): ItemStack {
    return itemStack(Material.PLAYER_HEAD) {
        val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(texture.toByteArray()), "WhaleSkull")
        val textures = profile.textures.apply {
            skin = getTextureURL(texture)
        }
        profile.setTextures(textures)
        meta {
            this as SkullMeta
            this.ownerProfile = profile
        }
        if (block != null) block(this)
    }
}

