package net.casual.championships.database.mongo

import net.casual.arcade.database.DatabaseElement
import net.casual.arcade.database.DatabaseObject
import org.bson.BsonDocument
import org.bson.BsonValue

class MongoObject(
    override val element: BsonDocument
): DatabaseObject<BsonValue> {
    override fun write(property: String, element: DatabaseElement<BsonValue>) {
        this.element[property] = element.element
    }
}