package cats

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import mainModule
import org.junit.Assert
import org.junit.Test
import asJson
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import kotlin.test.assertEquals

class CatsTest {
    @Test
    fun `Create Cat`(){
        withTestApplication(Application::mainModule) {
            val call = createCat("Fuzzy", 3)
            assertEquals(HttpStatusCode.Created, call.response.status())
        }
    }

    @Test
    fun `All Cats`(){
        withTestApplication(Application::mainModule) {
            val beforeCreate = handleRequest(HttpMethod.Get, "/cats")
            assertEquals("[]".asJson(), beforeCreate.response.content?.asJson())

            createCat("Shmuzy", 2)

            val afterCreate = handleRequest(HttpMethod.Get, "/cats")
            assertEquals("""[{"id":1,"name":"Shmuzy","age":2}]""".asJson(), afterCreate.response.content?.asJson())
        }
    }

    @Test
    fun `Cat by ID`() {
        withTestApplication(Application::mainModule) {
            val createCall = createCat("Apollo", 12)
            val id = createCall.response.content
            val afterCreate = handleRequest(Get, "/cats/$id")

            assertEquals("""{"id":1,"name":"Apollo","age":12}""".asJson(), afterCreate.response.content?.asJson())
        }
    }

    @Test
    fun `Delete Cat`(){
        withTestApplication(Application::mainModule) {
            val createCall = createCat("Apollo", 12)
            val id = createCall.response.content
            handleRequest(Delete, "/cats/$id")

            val afterDelete = handleRequest(Get, "/cats/$id")

            assertEquals(HttpStatusCode.NotFound, afterDelete.response.status())
        }
    }

    @Test
    fun `Update cat`() {
        withTestApplication(Application::mainModule) {
            val createCall = createCat("Puzzy", 3)
            val id = createCall.response.content
            handleRequest(HttpMethod.Put, "/cats/$id") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                setBody(listOf("name" to "Fuzzy", "age" to 4.toString()).formUrlEncode())
            }

            val afterUpdate = handleRequest(HttpMethod.Get, "/cats/$id")
            assertEquals("""{"id":1,"name":"Fuzzy","age":4}""".asJson(), afterUpdate.response.content?.asJson())
        }
    }

    @Before
    fun setup(){
        DB.connect()
        transaction {
            SchemaUtils.drop(Cats)
        }
    }
}

fun TestApplicationEngine.createCat(name: String, age:Int): TestApplicationCall{
    return handleRequest(HttpMethod.Post, "/cats"){
        addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        setBody(listOf("name" to name, "age" to age.toString()).formUrlEncode())
    }
}