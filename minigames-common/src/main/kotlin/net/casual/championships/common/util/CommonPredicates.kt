package net.casual.championships.common.util

import net.casual.arcade.visuals.predicate.EntityObserverPredicate.Companion.teammates
import net.casual.arcade.visuals.predicate.EntityObserverPredicate.Companion.visibleObservee
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate.Companion.toPlayer

@Suppress("JoinDeclarationAndAssignment")
object CommonPredicates {
    val VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES: PlayerObserverPredicate

    init {
        VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES = visibleObservee().and(
            PlayerObserverPredicate { _, observer -> observer.isSpectator }.or(teammates())
        ).toPlayer()
    }
}