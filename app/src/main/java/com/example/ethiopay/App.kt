package com.example.ethiopay

import android.app.Application
import company.tap.gosellapi.GoSellSDK

class App : Application() {
  override fun onCreate() {
    super.onCreate()

    GoSellSDK.init(this, "sk_test_xxx", packageName)
    GoSellSDK.setLocale("ar") // أو "ar"
  }
}