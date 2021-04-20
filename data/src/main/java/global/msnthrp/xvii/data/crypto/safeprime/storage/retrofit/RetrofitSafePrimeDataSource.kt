package global.msnthrp.xvii.data.crypto.safeprime.storage.retrofit

import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime
import global.msnthrp.xvii.data.crypto.safeprime.DefaultSafePrimeRepo
import global.msnthrp.xvii.data.network.Retrofit

class RetrofitSafePrimeDataSource : DefaultSafePrimeRepo.ReadOnlySafePrimeDataSource {

    private val apiService = Retrofit.safePrimeApiService

    override fun getSafePrime(): SafePrime {
        val safePrimeResponse = apiService.getSafePrime().execute().body()
        return safePrimeResponse?.toSafePrime() ?: SafePrime.EMPTY
    }

    private fun SafePrimeResponse.toSafePrime() =
            SafePrime(
                    p = p.base10,
                    q = q.base10,
                    g = g.base10
            )
}