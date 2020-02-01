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
     * ライブユーザー一覧を取得
     */
    fun getLiveUserList(): UserListResponse {
        val users = mutableMapOf<Long, UserResponse>()
        getLiveUserFromLiveDetailAPI(users)

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

        search("*", users)
        for (range in ranges) {
            range.forEach { ch ->
                search(ch.toString(), users)
            }
        }
    }

    fun search(str: String, users: MutableMap<Long, UserResponse>) {

        // 検索して雑に様々なユーザーを取得
        val result = mildomClient.getUserSearch(
                query = str,
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
                        status = userInfo.status,
                        fans = userInfo.fans ?: 0,
                        level = userInfo.level ?: 0,
                        viewer = userInfo.getViewers(),
                        official = userInfo.isOfficial()
                )
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
                                status = userInfo.status,
                                fans = userInfo.fans ?: 0,
                                level = userInfo.level ?: 0,
                                viewer = userInfo.getViewers(),
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

    /**
     * LiveDetailAPI より現在ライブしているユーザーを全取得
     */
    fun getLiveUserFromLiveDetailAPI(users: MutableMap<Long, UserResponse>) {

        // ライブユーザーを全部取得
        val result = mildomClient.getLiveUsers(
                key = "all_game",
                tag = "all",
                page = 1L,
                limit = 1000L,
                guestId = "guest",
                platform = "web",
                lang = "ja"
        )

        // オブジェクト情報を取得
        val userList = result.execute().body()?.body?.models
        if (userList != null) {

            for (userInfo in userList) {
                val i = userInfo.userId
                users[i] = UserResponse(
                        id = userInfo.userId,
                        name = userInfo.name,
                        status = userInfo.status,
                        fans = userInfo.fans ?: 0,
                        level = userInfo.level ?: 0,
                        viewer = userInfo.getViewers(),
                        official = userInfo.isOfficial()
                )
            }
        }
    }
}