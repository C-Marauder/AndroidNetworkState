package com.xhh.networkstate

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.*

class NetworkStateManager private constructor(private val application: Application) :
    LifecycleObserver , OnNetworkStateCallback {

    companion object {
        private const val TAG:String  = "NetworkStateManager"
        private lateinit var mNetworkManager: NetworkStateManager

        fun init(application: Application) {
            if (!Companion::mNetworkManager.isInitialized) {
                mNetworkManager = NetworkStateManager(application)
            }
            mNetworkManager.register()
        }

        fun registerNetworkStateCallback(lifecycleOwner: LifecycleOwner,onNetworkStateChange: (isConnectd:Boolean,networkType: Int?, networkName: String?) -> Unit){
            if (!Companion::mNetworkManager.isInitialized){
                Log.e(TAG,"please init NetworkStateManager")
            }else{
                mNetworkManager.registerNetworkStateCallback(lifecycleOwner,onNetworkStateChange)

            }
        }


    }
    internal var mCurrentNetworkType:Int?=null
    internal var mCurrentNetworkName:String?=null
    var isConnected:Boolean = false
    private val mLifecycleNetworkState:MutableList<LifecycleNetworkState> by lazy {
        mutableListOf<LifecycleNetworkState>()
    }

    /**
     * 注册网络状态监听
     */
    private fun register(){
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val telephonyManager = application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            if (connectivityManager.allNetworks.isNullOrEmpty()){
                isConnected = false
            }
            connectivityManager.registerNetworkCallback(networkRequest,object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.getNetworkCapabilities(network)?.let {
                        isConnected = true
                        val wifi = it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        if (wifi){
                            mCurrentNetworkType = NetworkCapabilities.TRANSPORT_WIFI
                            mCurrentNetworkName = wifiManager.connectionInfo.ssid

                        }
                        val mobile = it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        if (mobile){
                            mCurrentNetworkType = NetworkCapabilities.TRANSPORT_CELLULAR
                            mCurrentNetworkName = telephonyManager.networkOperatorName+validatePhoneNetworkType(telephonyManager.networkType)

                        }
                    }

                }


                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val wifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    if (wifi && mCurrentNetworkType == NetworkCapabilities.TRANSPORT_CELLULAR){
                        isConnected = true
                        mCurrentNetworkType = NetworkCapabilities.TRANSPORT_WIFI
                        mCurrentNetworkName = wifiManager.connectionInfo.ssid
                        onNetworkStateChange(isConnected,mCurrentNetworkType,mCurrentNetworkName)
                    }else{
                        val mobile = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        if (mobile && mCurrentNetworkType == NetworkCapabilities.TRANSPORT_WIFI){
                            isConnected = true
                            mCurrentNetworkType = NetworkCapabilities.TRANSPORT_CELLULAR
                            mCurrentNetworkName = telephonyManager.networkOperatorName+validatePhoneNetworkType(telephonyManager.networkType)
                        }
                        onNetworkStateChange(isConnected,mCurrentNetworkType,mCurrentNetworkName)
                    }



                }



                override fun onUnavailable() {
                    super.onUnavailable()
                    isConnected = false
                    mCurrentNetworkType = null
                    mCurrentNetworkName = null
                    onNetworkStateChange(isConnected,mCurrentNetworkType,mCurrentNetworkName)
                }


            })
        }else{

            val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            application.registerReceiver(NetworkStateReceiver(), intentFilter)
        }

    }

    /**
     * 判断手机网络类型
     */
    private fun validatePhoneNetworkType(phoneNetworkType:Int):String{
        return if (phoneNetworkType <TelephonyManager.NETWORK_TYPE_CDMA){
            "2G"
        } else if (phoneNetworkType >=TelephonyManager.NETWORK_TYPE_CDMA && phoneNetworkType<TelephonyManager.NETWORK_TYPE_LTE){
            "3G"
        } else if (phoneNetworkType>=TelephonyManager.NETWORK_TYPE_LTE && phoneNetworkType<TelephonyManager.NETWORK_TYPE_NR){
            "4G"
        }else{
            "5G"
        }
    }

    fun registerNetworkStateCallback(lifecycleOwner: LifecycleOwner,onNetworkStateChange:(isConnected:Boolean,networkType:Int?,networkName:String?)->Unit){

        val lifecycleNetworkState = LifecycleNetworkState(this,lifecycleOwner,onNetworkStateChange)
        mLifecycleNetworkState.add(lifecycleNetworkState)


    }

    internal fun remove(lifecycleNetworkState: LifecycleNetworkState){
        mLifecycleNetworkState.remove(lifecycleNetworkState)
    }

    override fun onNetworkStateChange(isConnected:Boolean,networkType: Int?, networkName: String?) {
        mLifecycleNetworkState.forEach {
            it.dispatch(isConnected,networkType,networkName)
        }
    }

    private inner class NetworkStateReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == ConnectivityManager.CONNECTIVITY_ACTION){
                    context?.getSystemService(Context.CONNECTIVITY_SERVICE)?.let {cm->
                        cm as ConnectivityManager
                        cm.activeNetworkInfo?.let {networkInfo->
                            isConnected = networkInfo.isConnectedOrConnecting
                            mCurrentNetworkType = networkInfo.type
                            mCurrentNetworkName = networkInfo.typeName
                            mLifecycleNetworkState.forEach {lnt->
                                lnt.dispatch(isConnected,mCurrentNetworkType,mCurrentNetworkName)
                            }
                        }
                    }
                }
            }
        }

    }
}



