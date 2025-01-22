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
package net.ccbluex.liquidbounce.features.module.modules.misc

import it.unimi.dsi.fastutil.ints.Int2LongLinkedOpenHashMap
import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.config.types.NamedChoice
import net.ccbluex.liquidbounce.event.events.AttackEntityEvent
import net.ccbluex.liquidbounce.event.events.NotificationEvent
import net.ccbluex.liquidbounce.event.events.TagEntityEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.client.notification
import net.minecraft.client.network.AbstractClientPlayerEntity

/**
 * Focus module
 *
 * Filters out any other entity to be targeted except the one focus is set to
 */
object ModuleFocus : ClientModule("Focus", Category.MISC) {

    private val mode = choices("Mode", Filter, arrayOf(Temporary, Filter))

    /**
     * This option will only focus the enemy on combat modules
     */
    private val combatOnly by boolean("Combat", false)

    private sealed class FocusChoice(name: String) : Choice(name) {
        override val parent: ChoiceConfigurable<*>
            get() = mode
        abstract fun isFocused(playerEntity: AbstractClientPlayerEntity): Boolean
    }

    private object Filter : FocusChoice("Filter") {

        private val usernames by textArray("Usernames", mutableListOf("Notch"))
        private val filterType by enumChoice("FilterType", FilterType.WHITELIST)

        enum class FilterType(override val choiceName: String) : NamedChoice {
            WHITELIST("Whitelist"),
            BLACKLIST("Blacklist")
        }

        override fun isFocused(playerEntity: AbstractClientPlayerEntity): Boolean {
            val name = playerEntity.gameProfile.name

            return when (filterType) {
                FilterType.WHITELIST -> usernames.any { it.equals(name, true) }
                FilterType.BLACKLIST -> usernames.none {
                    it.equals(name, true)
                }
            }

        }
    }

    private object Temporary : FocusChoice("Temporary") {

        private val timeUntilReset by int("MaximumTime", 30, 0..120, "s")
        private val outOfRange by float("MaximumRange", 20f, 8f..40f)

        private val whenNoFocus by enumChoice("WhenNoFocus", NoFocusMode.ALLOW_ALL)

        // Combination of [entityId] and [time]
        private val focus = Int2LongLinkedOpenHashMap()

        enum class NoFocusMode(override val choiceName: String) : NamedChoice {
            ALLOW_ALL("AllowAll"),
            ALLOW_NONE("AllowNone")
        }

        @Suppress("unused")
        private val attackHandler = handler<AttackEntityEvent> { event ->
            val target = event.entity as? AbstractClientPlayerEntity ?: return@handler

            if (!focus.containsKey(target.id)) {
                notification(
                    "Focus",
                    message("focused", target.gameProfile.name, timeUntilReset),
                    NotificationEvent.Severity.INFO
                )
            }
            focus.put(target.id, System.currentTimeMillis() + timeUntilReset * 1000)
        }

        @Suppress("unused")
        private val cleanUpTask = tickHandler {
            if (player.isDead) {
                focus.clear()
                return@tickHandler
            }

            val currentTime = System.currentTimeMillis()
            focus.int2LongEntrySet().removeIf { (entityId, time) ->
                // Remove if entity is out of range
                val entity = world.getEntityById(entityId) as? AbstractClientPlayerEntity ?: return@removeIf true

                // Remove if time is up
                if (time < currentTime) {
                    notification(
                        "Focus",
                        message("timeUp", entity.gameProfile.name),
                        NotificationEvent.Severity.INFO
                    )
                    return@removeIf true
                }

                if (entity.squaredDistanceTo(player) > outOfRange * outOfRange) {
                    notification(
                        "Focus",
                        message("outOfRange", entity.gameProfile.name),
                        NotificationEvent.Severity.INFO
                    )
                    return@removeIf true
                }

                false
            }
        }

        override fun isFocused(playerEntity: AbstractClientPlayerEntity): Boolean {
            val entityId = playerEntity.id

            if (focus.isEmpty()) {
                return when (whenNoFocus) {
                    NoFocusMode.ALLOW_ALL -> true
                    NoFocusMode.ALLOW_NONE -> false
                }
            }

            return focus.containsKey(entityId)
        }

    }

    @Suppress("unused")
    private val tagEntityEvent = handler<TagEntityEvent> {
        if (it.entity !is AbstractClientPlayerEntity || isInFocus(it.entity)) {
            return@handler
        }

        if (combatOnly) {
            it.dontTarget()
        } else {
           it.ignore()
        }
    }

    /**
     * Check if [entity] is in your focus
     */
    private fun isInFocus(entity: AbstractClientPlayerEntity): Boolean {
        if (!running) {
            return false
        }

        return mode.activeChoice.isFocused(entity)
    }

}
