package {{PACKAGE_NAME}}.core.network

import {{PACKAGE_NAME}}.core.network.model.NetworkSampleItem
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Sample API service. Replace with your own endpoints.
 */
interface SampleApi {

    @GET("items")
    suspend fun getItems(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): List<NetworkSampleItem>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: String): NetworkSampleItem
}
