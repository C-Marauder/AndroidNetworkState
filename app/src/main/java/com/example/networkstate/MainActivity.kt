package com.example.networkstate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xhh.networkstate.NetworkStateManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NetworkStateManager.registerNetworkStateCallback(this){
            isConnectd, networkType, networkName ->
            //isConnected 是否连接，networkType 网络类型 WIFI or Mobile，networkName 网络的名称

        }
    }
}
