package net.casual.championships.database.mongo

import net.casual.arcade.database.DatabaseElement
import org.bson.BsonValue

class MongoElement(
    override val element: BsonValue
): DatabaseElement<BsonValue>