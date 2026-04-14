package com.kbyai.idcardrecognition.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kbyai.idcardrecognition.login.AuthSessionGateway
import com.kbyai.idcardrecognition.login.AuthSessionRepository
import com.kbyai.idcardrecognition.login.AuthViewModel
import com.kbyai.idcardrecognition.login.PhoneAuthGateway
import com.kbyai.idcardrecognition.login.PhoneAuthRepository
import com.kbyai.idcardrecognition.noble.data.FirestoreNobleCloudStore
import com.kbyai.idcardrecognition.noble.data.NobleCloudStore
import com.kbyai.idcardrecognition.noble.data.NobleRepository
import com.kbyai.idcardrecognition.noble.data.NobleRepositoryImpl
import com.kbyai.idcardrecognition.noble.data.local.NobleDatabase
import com.kbyai.idcardrecognition.noble.ui.home.NobleHomeViewModel
import com.kbyai.idcardrecognition.payment.demo.DemoApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single<PhoneAuthGateway> { PhoneAuthRepository(get()) }
    single<NobleCloudStore> { FirestoreNobleCloudStore(get()) }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    single {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(DemoApi.baseUrl)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<AuthSessionGateway> { AuthSessionRepository(get(), get(), get()) }

    single {
        Room.databaseBuilder(
            get(),
            NobleDatabase::class.java,
            "noble-cache.db",
        ).fallbackToDestructiveMigration().build()
    }

    single { get<NobleDatabase>().nobleDao() }
    single<NobleRepository> { NobleRepositoryImpl(get(), get()) }
    viewModel { AuthViewModel(get(), get()) }
    viewModel { (userId: String) -> NobleHomeViewModel(userId = userId, repository = get()) }
}
