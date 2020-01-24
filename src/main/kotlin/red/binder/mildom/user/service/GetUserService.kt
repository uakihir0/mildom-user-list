package red.binder.mildom.user.service

import org.springframework.stereotype.Component
import red.binder.mildom.user.client.MildomClient
import red.binder.mildom.user.model.UserListResponse
import red.binder.mildom.user.model.UserResponse
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicLong

@Component
class GetUserService(
    private val mildomClient: MildomClient
) {

    /**
     * ユーザーの一覧を取得
     */
    fun getUserList(): UserListResponse {
        val users = mutableListOf<UserResponse>()

        // スレッドを複数立てて一気に取得
        val threadPool = Executors.newCachedThreadPool() as ThreadPoolExecutor

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
                    val i = index.incrementAndGet()
                    println("get: $i")

                    // Mildom からユーザー情報を取得
                    val user = mildomClient.getUserProfile(
                        userId = i,
                        guestId = "guest",
                        platform = "web",
                        lang = "ja"
                    )

                    // オブジェクト情報を取得
                    val userInfo = user.execute().body()?.body?.userInfo


                    if (userInfo != null) {
                        error.set(0L)

                        // 問題ないアカウントの場合のみ
                        if (userInfo.status == 10 &&
                            (userInfo.fans ?: 0) >= 50) {

                            users.add(
                                UserResponse(
                                    id = userInfo.userId,
                                    name = userInfo.name,
                                    fans = userInfo.fans ?: 0,
                                    official = userInfo.isOfficial()
                                )
                            )
                        }
                    } else {
                        val e = error.incrementAndGet()
                        println("error $e index $i")

                        // 100 回以上連続
                        if (e >= 100) {

                            // データが無い場合は終点
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

        // フォロワーの数で降順ソート
        users.sortBy { it.fans }
        users.reverse()

        return UserListResponse(users)
    }
}