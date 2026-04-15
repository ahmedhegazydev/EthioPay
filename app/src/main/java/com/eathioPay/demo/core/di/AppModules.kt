package com.eathioPay.demo.core.di

import com.eathioPay.demo.core.common.AppDispatchers
import com.eathioPay.demo.core.network.createOkHttpClient
import com.eathioPay.demo.core.network.createRetrofit
import com.eathioPay.demo.core.session.DataStoreSessionStore
import com.eathioPay.demo.core.session.SessionStore
import com.eathioPay.demo.feature.auth.data.remote.AuthApiService
import com.eathioPay.demo.feature.auth.data.repository.AuthRepositoryImpl
import com.eathioPay.demo.feature.auth.domain.repository.AuthRepository
import com.eathioPay.demo.feature.auth.domain.usecase.ObserveSessionUseCase
import com.eathioPay.demo.feature.auth.domain.usecase.RequestOtpUseCase
import com.eathioPay.demo.feature.auth.domain.usecase.ResolveGateDestinationUseCase
import com.eathioPay.demo.feature.auth.domain.usecase.VerifyOtpUseCase
import com.eathioPay.demo.feature.auth.presentation.AuthFeatureViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit

val appModule = module {
    single { AppDispatchers() }
    single<SessionStore> { DataStoreSessionStore(androidContext()) }

    single<OkHttpClient> { createOkHttpClient() }
    single<Retrofit> { createRetrofit(get()) }

    single<AuthApiService> { get<Retrofit>().create(AuthApiService::class.java) }
    single<AuthRepository> { AuthRepositoryImpl(api = get(), sessionStore = get(), dispatchers = get()) }

    factory { RequestOtpUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { ObserveSessionUseCase(get()) }
    factory { ResolveGateDestinationUseCase() }

    viewModelOf(::AuthFeatureViewModel)
}
