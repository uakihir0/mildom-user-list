package red.binder.mildom.user.config

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import red.binder.mildom.user.client.MildomClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Configuration
class MildomConfig {

    @Bean
    fun getMildomClient(): MildomClient {
        val gson = GsonBuilder()
            .serializeNulls()
            .create()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("https://cloudac.mildom.com/")
            .client(OkHttpClient.Builder().build())
            .build()

        return retrofit.create(MildomClient::class.java)
    }
}