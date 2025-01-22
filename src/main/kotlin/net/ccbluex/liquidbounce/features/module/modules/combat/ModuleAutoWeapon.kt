/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.types.NamedChoice
import net.ccbluex.liquidbounce.event.events.AttackEntityEvent
import net.ccbluex.liquidbounce.event.sequenceHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.combat.ModuleAutoWeapon.againstShield
import net.ccbluex.liquidbounce.features.module.modules.combat.ModuleAutoWeapon.prepare
import net.ccbluex.liquidbounce.utils.inventory.HotbarItemSlot
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.ItemCategorization
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.items.WeaponItemFacet
import net.ccbluex.liquidbounce.utils.client.SilentHotbar
import net.ccbluex.liquidbounce.utils.client.isOlderThanOrEqual1_8
import net.ccbluex.liquidbounce.utils.inventory.Slots
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.SwordItem

/**
 * AutoWeapon module
 *
 * Automatically selects the best weapon in your hotbar
 */
object ModuleAutoWeapon : ClientModule("AutoWeapon", Category.COMBAT) {

    /**
     * The weapon type to prefer, which is on 1.8 and 1.9+ versions usually a sword,
     * due to the attack speed.
     *
     * On 1.9+ we are likely to prefer an axe when the target is blocking with a shield,
     * which is covered by the [againstShield] weapon type.
     */
    private val preferredWeapon by enumChoice("Preferred", WeaponType.SWORD)
    private val againstShield by enumChoice("BlockedByShield", WeaponType.AXE)

    @Suppress("unused")
    enum class WeaponType(
        override val choiceName: String,
        val filter: (WeaponItemFacet) -> Boolean
    ): NamedChoice {
        ANY("Any", { true }),
        SWORD("Sword", { it.itemStack.item is SwordItem }),
        AXE("Axe", { it.itemStack.item is AxeItem }),

        /**
         * Do not prefer any weapon type, this is useful to only
         * use the [againstShield] weapon type.
         */
        NONE("None", { false })
    }

    private val prepare by boolean("Prepare", true)

    /**
     * In Minecraft, even when we send a packet to switch the slot,
     * before we attack, the server will still calculate damage
     * based on the slot we were on before the switch.
     *
     * This is why we have to wait at least 1 tick before attacking
     * after switching the slot.
     *
     * This is not necessary when we are already on the correct slot,
     * which should be the case when using [prepare].
     */
    private val switchOn by int("SwitchOn", 1, 0..2, "ticks")
    private val switchBack by int("SwitchBack", 20, 1..300, "ticks")

    @Suppress("unused")
    private val attackHandler = sequenceHandler<AttackEntityEvent> { event ->
        val entity = event.entity as? LivingEntity ?: return@sequenceHandler
        val weaponSlot = determineWeaponSlot(entity) ?: return@sequenceHandler
        val isOnSwitch = SilentHotbar.serversideSlot != weaponSlot

        SilentHotbar.selectSlotSilently(
            this,
            weaponSlot,
            switchBack
        )

        // Sync selected slot right now,
        // we will not sync on this tick otherwise
        interaction.syncSelectedSlot()

        if (isOnSwitch && switchOn > 0) {
            event.cancelEvent()

            // Re-attack after switch
            // This should not end up in a recursive loop,
            // because we switched slot by now
            waitTicks(switchOn)
            event.caller()
        }
    }

    /**
     * Prepare AutoWeapon for given [entity] if [prepare] is enabled
     */
    fun prepare(entity: Entity?) {
        if (!running || !prepare || entity !is LivingEntity) {
            return
        }

        determineWeaponSlot(entity)?.let { slot ->
            SilentHotbar.selectSlotSilently(
                this,
                slot,
                switchBack
            )
        }
    }

    private fun determineWeaponSlot(target: LivingEntity?): Int? {
        val itemCategorization = ItemCategorization(Slots.Hotbar)
        val itemMap = Slots.Hotbar
            .flatMap { itemCategorization.getItemFacets(it).filterIsInstance<WeaponItemFacet>().toList() }

        val bestSlot = itemMap
            .filter(
                when {
                    !isOlderThanOrEqual1_8 && target?.blockedByShield(world.damageSources.playerAttack(player)) == true
                        -> againstShield.filter

                    else -> preferredWeapon.filter
                }
            )
            .maxOrNull()

        return (bestSlot?.itemSlot as HotbarItemSlot?)?.hotbarSlot
    }

}
