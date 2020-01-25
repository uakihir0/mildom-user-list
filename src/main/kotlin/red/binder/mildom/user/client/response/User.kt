package red.binder.mildom.user.client.response

import com.google.gson.annotations.SerializedName

data class User(
        @SerializedName("user_id")
        var userId: Long,

        @SerializedName("loginname")
        var name: String,

        @SerializedName("status")
        var status: Int,

        @SerializedName("fans")
        var fans: Long?,

        @SerializedName("viewers")
        var viewers: Long?,

        @SerializedName("level")
        var level: Long?,

        @SerializedName("live_type")
        var liveType: Int?,

        /** 公認名 */
        @SerializedName("certification_intro")
        var certificationIntro: String?
) {

    /** 公認配信者かどうか？ */
    fun isOfficial(): Boolean {
        return (certificationIntro != null)
    }

    /** 視聴者を取得 */
    fun getViewers(): Long {
        if (liveType == null) return 0
        return viewers ?: 0
    }
}