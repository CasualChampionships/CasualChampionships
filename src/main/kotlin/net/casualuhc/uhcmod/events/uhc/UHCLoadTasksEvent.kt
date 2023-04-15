package net.casualuhc.uhcmod.events.uhc

import net.casualuhc.arcade.events.core.Event
import net.casualuhc.uhcmod.task.SavableTask

class UHCLoadTasksEvent: Event() {
    private val tasks = LinkedHashMap<String, SavableTask>()

    fun add(task: SavableTask): UHCLoadTasksEvent {
        this.tasks[task.id] = task
        return this
    }

    fun get(id: String): SavableTask? {
        return this.tasks[id]
    }
}