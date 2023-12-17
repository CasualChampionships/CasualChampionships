package net.casual.championships.database

import net.casual.arcade.database.DatabaseWriter
import net.casual.championships.database.mongo.MongoCollection
import net.casual.championships.database.mongo.MongoElement
import net.casual.championships.database.mongo.MongoObject
import org.bson.*

class MongoDatabaseWriter: DatabaseWriter<BsonValue> {
    override fun createEmptyObject(): MongoObject {
        return MongoObject(BsonDocument())
    }

    override fun createEmptyCollection(): MongoCollection {
        return MongoCollection(BsonArray())
    }

    override fun createStringElement(string: String): MongoElement {
        return MongoElement(BsonString(string))
    }

    override fun createDoubleElement(double: Double): MongoElement {
        return MongoElement(BsonDouble(double))
    }

    override fun createLongElement(long: Long): MongoElement {
        return MongoElement(BsonInt64(long))
    }

    override fun createIntegerElement(integer: Int): MongoElement {
        return MongoElement(BsonInt32(integer))
    }

    override fun createBooleanElement(boolean: Boolean): MongoElement {
        return MongoElement(BsonBoolean(boolean))
    }

    override fun createEmptyElement(): MongoElement {
        return MongoElement(BsonNull.VALUE)
    }
}