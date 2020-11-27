import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import mainModule

class ApplicationTest {

    @Test
    fun emptyPath(){
        withTestApplication(Application::mainModule) {
            val call = handleRequest(HttpMethod.Get, "")
            assertEquals(HttpStatusCode.OK, call.response.status())
        }

    }

    @Test
    fun validValue(){
        withTestApplication(Application::mainModule) {
            val call = handleRequest(HttpMethod.Get, "/Snowflake")

            assertEquals("""
                {
                  "Cat name:" : "Snowflake"
                }
                """.asJson(), call.response.content?.asJson())
        }

    }
}


fun String.asJson() = ObjectMapper().readTree(this)