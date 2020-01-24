package red.binder.mildom.user.service

import org.springframework.stereotype.Component
import red.binder.mildom.user.client.MildomClient
import red.binder.mildom.user.model.UserListResponse
import red.binder.mildom.user.model.UserResponse
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

@Component
class GetUserService(
        private val mildomClient: MildomClient
) {

    /**
     * ユーザーの一覧を取得
     */
    fun getUserList(): UserListResponse {
        val users = mutableMapOf<Long, UserResponse>()
        getUserFromSearchAPI(users)
        getUserFromProfileAPI(users)

        // フォロワーの数で降順
        return UserListResponse(users.values
                .filter { it.fans >= 50 }
                .filter { it.status == 10 }
                .sortedByDescending { it.fans })
    }

    /**
     * SearchAPI から雑にユーザーを取得
     */
    fun getUserFromSearchAPI(users: MutableMap<Long, UserResponse>) {
        val ranges: List<CharRange> = listOf(
                'a'..'z', '0'..'9', 'あ'..'ん', 'ア'..'ン')

        for (range in ranges) {
            range.forEach { ch ->
                println(ch.toString())

                // 検索して雑に様々なユーザーを取得
                val result = mildomClient.getUserSearch(
                        query = ch.toString(),
                        type = 1L,
                        limit = 1000L,
                        guestId = "guest",
                        platform = "web",
                        lang = "ja"
                )

                // オブジェクト情報を取得
                val userList = result.execute().body()?.body?.users
                if (userList != null) {

                    for (userInfo in userList) {
                        val i = userInfo.userId
                        users[i] = UserResponse(
                                id = userInfo.userId,
                                name = userInfo.name,
                                fans = userInfo.fans ?: 0,
                                level = userInfo.level ?: 0,
                                status = userInfo.status,
                                official = userInfo.isOfficial()
                        )
                    }
                }
            }
        }
    }


    /**
     * ProfileAPI からユーザーを取得
     */
    fun getUserFromProfileAPI(users: MutableMap<Long, UserResponse>) {

        // スレッドを複数立てて一気に取得
        val threadPool = Executors.newCachedThreadPool()

        // 初期インデックス
        val index = AtomicLong(10000000L)
        val error = AtomicLong(0L)
        val batch = 5

        // 終端を発見するまで実行
        var findEnds = false
        while (!findEnds) {
            val latch = CountDownLatch(batch)

            // 適度にやめる
            if (10010000 < index.get()) {
                break
            }

            repeat(batch) {
                threadPool.submit {

                    // まだ存在しないユーザー取得
                    var i = index.incrementAndGet()
                    while (users.containsKey(i)) {
                        i = index.incrementAndGet()
                    }

                    println("index $i")

                    // Mildom からユーザー情報を取得
                    val result = mildomClient.getUserProfile(
                            userId = i,
                            guestId = "guest",
                            platform = "web",
                            lang = "ja"
                    )

                    // オブジェクト情報を取得
                    val userInfo = result.execute().body()?.body?.userInfo
                    if (userInfo != null) {
                        error.set(0L)
                        users[i] = UserResponse(
                                id = userInfo.userId,
                                name = userInfo.name,
                                fans = userInfo.fans ?: 0,
                                level = userInfo.level ?: 0,
                                status = userInfo.status,
                                official = userInfo.isOfficial()
                        )

                    } else {
                        val e = error.incrementAndGet()
                        println("error $e index $i")

                        // 100 回以上連続の場合は終点
                        if (e >= 100) {
                            findEnds = true
                        }
                    }

                    // カウントダウン
                    latch.countDown()
                }
            }

            // リクエストを待つ
            latch.await()
        }
    }
}