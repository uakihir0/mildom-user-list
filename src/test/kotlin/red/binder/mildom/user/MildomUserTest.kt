package red.binder.mildom.user

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import red.binder.mildom.user.service.GetUserService
import java.io.File

@RunWith(SpringRunner::class)
@SpringBootTest
class MildomUserTest {

    @Autowired
    lateinit var getUserService: GetUserService

    @Test
    fun makeDataFiles() {
        val mapper = ObjectMapper()
        val users = getUserService.getUserList()

        val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(users)
        val js = "var mildom_users = $json;"

        File("./page/data.json").writeText(json)
        File("./page/data.js").writeText(js)
    }
}
