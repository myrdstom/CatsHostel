package cats

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

interface CatsService{
    suspend fun create(name: String, age: Int?): Int

    suspend fun all(): List<Cat>

    suspend fun findById(id: Int): Cat?

    suspend fun delete(id: Int)

    suspend fun update(id: Int, name: String, age: Int?)
}

class CatsServiceDB : CatsService {

    override suspend fun update(id: Int, name: String, age: Int?) {
        transaction {
            Cats.update({ Cats.id eq id }) {
                it[Cats.name] = name
                if (age != null) {
                    it[Cats.age] = age
                }
            }
        }
    }

    override suspend fun delete(id: Int){
        transaction {
            Cats.deleteWhere {
                Cats.id eq id
            }
        }
    }

    override suspend fun findById(id: Int): Cat? {
        val row = transaction {
            addLogger(StdOutSqlLogger)
            Cats.select {
                Cats.id eq id // select cats.id, cats.name, cats.age from catsw where cats.id = 1
            }.firstOrNull()
        }
        return row?.asCat()
    }

    override suspend fun all(): List<Cat>{
        return transaction {
            Cats.selectAll().map { row ->
//                Convert db row to data class
                row.asCat()

            }
        }

    }
    override  suspend fun create(name: String, age: Int?): Int{
        val id = transaction{
            Cats.insertAndGetId { cat ->
                cat[Cats.name] = name
                if (age != null){
                    cat[Cats.age] = age
                }

            }
        }
        return id.value
    }

    private fun ResultRow.asCat() = Cat(
        this[Cats.id].value,
        this[Cats.name],
        this[Cats.age]
    )
}



//Defining the datatypes from the db
data class Cat(val id: Int, val name: String, val age: Int)