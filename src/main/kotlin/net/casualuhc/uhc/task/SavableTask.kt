package net.casualuhc.uhc.task

import net.casualuhc.arcade.scheduler.Task

abstract class SavableTask: Task() {
    abstract val id: String
}