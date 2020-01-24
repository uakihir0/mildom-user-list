package red.binder.mildom.user.client

import red.binder.mildom.user.client.response.Response
import red.binder.mildom.user.client.response.UserInfo
import red.binder.mildom.user.client.response.UserList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MildomClient {

    /**
     * ユーザーの詳細情報を取得
     *
     * https://cloudac.mildom.com/nonolive/gappserv/user/profileV2?user_id=10000000&__guest_id=guest&__platform=web&__la=ja
     */
    @GET("nonolive/gappserv/user/profileV2")
    fun getUserProfile(
            @Query("user_id") userId: Long,
            @Query("__guest_id") guestId: String,
            @Query("__platform") platform: String,
            @Query("__la") lang: String
    ): Call<Response<UserInfo>>

    /**
     * ユーザーの検索
     *
     * https://cloudac.mildom.com/nonolive/gsearch/search?type=1&query=a&page=1&limit=1000&__guest_id=guest&__platform=web&__la=ja
     */
    @GET("nonolive/gsearch/search")
    fun getUserSearch(
            @Query("query") query: String,
            @Query("type") type: Long,
            @Query("limit") limit: Long,
            @Query("__guest_id") guestId: String,
            @Query("__platform") platform: String,
            @Query("__la") lang: String
    ): Call<Response<UserList>>
}