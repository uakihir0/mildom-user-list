package red.binder.mildom.user

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import red.binder.mildom.user.model.UserListResponse
import red.binder.mildom.user.service.GetUserService
import java.io.File

@RunWith(SpringRunner::class)
@SpringBootTest
class MildomUserTest {

    @Autowired
    lateinit var getUserService: GetUserService

    @Test
    fun testGetAllUserData() {
        val result = getUserService.getUserList()

        val dataJson = File("./page/data.json")
        val dataJs = File("./page/data.js")

        // 先にオブジェクトを展開
        val userList = Gson().fromJson(dataJson.readText(), UserListResponse::class.java)
        val userMap = userList.users.associateBy { it.id }

        // 結果で取得した情報に視聴者情報を格納
        for (user in result.users) {
            if (userMap.containsKey(user.id)) {
                user.viewer = (userMap[user.id]?.viewer ?: 0)
            }
        }

        val json = GsonBuilder().setPrettyPrinting().create().toJson(result)
        val js = "var mildom_users = $json;"

        dataJson.writeText(json)
        dataJs.writeText(js)
    }

    @Test
    fun testGetUserLiveData() {
        val result = getUserService.getLiveUserList()

        val dataJson = File("./page/data.json")
        val dataJs = File("./page/data.js")

        // 先にオブジェクトを展開
        val userList = Gson().fromJson(dataJson.readText(), UserListResponse::class.java)
        val userMap = userList.users.associateBy { it.id }

        // 結果で取得した情報に視聴者情報を格納
        for (user in result.users) {

            // 配信中アカウントのみ更新
            val newValue = user.viewer
            if (newValue > 0) {

                if (userMap.containsKey(user.id)) {
                    val oldValue = (userMap[user.id]?.viewer ?: 0)
                    val nextValue = ((newValue * 0.2) + (oldValue * 0.8))
                    userMap[user.id]?.viewer = nextValue.toLong()
                }
            }
        }

        // 書き換えた情報をそのまま書き戻す
        val json = GsonBuilder().setPrettyPrinting().create().toJson(userList)
        val js = "var mildom_users = $json;"

        dataJson.writeText(json)
        dataJs.writeText(js)
    }
}
