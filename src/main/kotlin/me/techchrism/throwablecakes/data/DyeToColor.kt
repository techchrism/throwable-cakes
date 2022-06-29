package me.techchrism.throwablecakes.data

import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material

class DyeToColor {
    companion object {
        val mapping = mapOf(
            Material.WHITE_DYE to DyeColor.WHITE,
            Material.ORANGE_DYE to DyeColor.ORANGE,
            Material.MAGENTA_DYE to DyeColor.MAGENTA,
            Material.LIGHT_BLUE_DYE to DyeColor.LIGHT_BLUE,
            Material.YELLOW_DYE to DyeColor.YELLOW,
            Material.LIME_DYE to DyeColor.LIME,
            Material.PINK_DYE to DyeColor.PINK,
            Material.GRAY_DYE to DyeColor.GRAY,
            Material.LIGHT_GRAY_DYE to DyeColor.LIGHT_GRAY,
            Material.CYAN_DYE to DyeColor.CYAN,
            Material.PURPLE_DYE to DyeColor.PURPLE,
            Material.BLUE_DYE to DyeColor.BLUE,
            Material.BROWN_DYE to DyeColor.BROWN,
            Material.GREEN_DYE to DyeColor.GREEN,
            Material.RED_DYE to DyeColor.RED,
            Material.BLACK_DYE to DyeColor.BLACK
        )

        val chat = mapOf(
            DyeColor.WHITE to ChatColor.WHITE,
            DyeColor.ORANGE to ChatColor.GOLD,
            DyeColor.MAGENTA to ChatColor.DARK_PURPLE,
            DyeColor.LIGHT_BLUE to ChatColor.AQUA,
            DyeColor.YELLOW to ChatColor.YELLOW,
            DyeColor.LIME to ChatColor.GREEN,
            DyeColor.PINK to ChatColor.LIGHT_PURPLE,
            DyeColor.GRAY to ChatColor.DARK_GRAY,
            DyeColor.LIGHT_GRAY to ChatColor.GRAY,
            DyeColor.CYAN to ChatColor.DARK_AQUA,
            DyeColor.PURPLE to ChatColor.DARK_PURPLE,
            DyeColor.BLUE to ChatColor.BLUE,
            DyeColor.BROWN to ChatColor.DARK_GRAY,
            DyeColor.GREEN to ChatColor.DARK_GREEN,
            DyeColor.RED to ChatColor.RED,
            DyeColor.BLACK to ChatColor.BLACK
        )
    }
}