package com.xhh.networkstate

import android.os.Looper
import androidx.lifecycle.*


internal class LifecycleNetworkState(
    private val networkStateManager: NetworkStateManager,
    private val lifecycleOwner: LifecycleOwner,
    private val onNetworkStateChange:(isConnected:Boolean,networkType:Int?,networkName:String?)->Unit
) : LifecycleObserver, Observer<NetworkState> {
    private var mNetworkStateLiveDate:MutableLiveData<NetworkState>?=null
    private lateinit var mNetworkState: NetworkState
    init {
        lifecycleOwner.lifecycle.addObserver(this)
        mNetworkStateLiveDate = MutableLiveData()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        mNetworkState = NetworkState(networkStateManager.isConnected,networkStateManager.mCurrentNetworkType,networkStateManager.mCurrentNetworkName)
        mNetworkStateLiveDate?.let {
            it.observe(lifecycleOwner, this)
            it.value = mNetworkState
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mNetworkStateLiveDate?.removeObserver(this)
        networkStateManager.remove(this)
    }

    override fun onChanged(t: NetworkState?) {
        t?.let {
            onNetworkStateChange(t.isConnected,it.networkType,it.networkName)
        }

    }

    fun dispatch(isConnected:Boolean,type:Int?,name:String?){
        mNetworkState.let {
            it.isConnected = isConnected
            it.networkType = type
            it.networkName = name
        }

        if (Looper.getMainLooper() == Looper.myLooper()){
            mNetworkStateLiveDate?.value = mNetworkState
        }else{
            mNetworkStateLiveDate?.postValue(mNetworkState)
        }

    }
}

