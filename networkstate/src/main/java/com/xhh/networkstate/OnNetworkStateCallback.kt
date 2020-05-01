package com.xhh.networkstate


internal interface OnNetworkStateCallback {

    fun onNetworkStateChange(isConnected: Boolean,networkType: Int?,networkName: String?)
}

data class NetworkState(var isConnected:Boolean = false,var networkType:Int?=null,var networkName:String?=null)

