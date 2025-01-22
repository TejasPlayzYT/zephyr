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
package net.ccbluex.liquidbounce.api.services.cosmetics

import net.ccbluex.liquidbounce.api.core.API_V3_ENDPOINT
import net.ccbluex.liquidbounce.api.core.BaseApi
import net.ccbluex.liquidbounce.api.models.cosmetics.Cosmetic
import java.util.*

object CosmeticApi : BaseApi(API_V3_ENDPOINT) {

    suspend fun getCarriers() =
        get<Set<String>>("/cosmetics/carriers")

    suspend fun getCarrierCosmetics(uuid: UUID) =
        get<Set<Cosmetic>>("/cosmetics/carrier/$uuid")

}
