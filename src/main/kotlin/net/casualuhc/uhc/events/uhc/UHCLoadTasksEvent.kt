package net.casualuhc.uhc.events.uhc

import net.casualuhc.arcade.events.core.Event
import net.casualuhc.uhc.task.SavableTask

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