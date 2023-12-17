package net.casual.championships.database.mongo

import net.casual.arcade.database.DatabaseCollection
import net.casual.arcade.database.DatabaseElement
import org.bson.BsonArray
import org.bson.BsonValue

class MongoCollection(
    override val element: BsonArray
): DatabaseCollection<BsonValue> {
    override fun add(element: DatabaseElement<BsonValue>) {
        this.element.add(element.element)
    }
}