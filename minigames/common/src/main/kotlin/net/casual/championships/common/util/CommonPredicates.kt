package net.casual.championships.common.util

import net.casual.arcade.gui.predicate.EntityObserverPredicate.Companion.observerInGamemode
import net.casual.arcade.gui.predicate.EntityObserverPredicate.Companion.teammates
import net.casual.arcade.gui.predicate.EntityObserverPredicate.Companion.visibleObservee
import net.casual.arcade.gui.predicate.PlayerObserverPredicate
import net.casual.arcade.gui.predicate.PlayerObserverPredicate.Companion.toPlayer
import net.minecraft.world.level.GameType

@Suppress("JoinDeclarationAndAssignment")
object CommonPredicates {
    val VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES: PlayerObserverPredicate

    init {
        VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES = visibleObservee().and(
            observerInGamemode(GameType.SPECTATOR).or(teammates())
        ).toPlayer()
    }
}