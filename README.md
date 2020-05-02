# AndroidNetworkState
Android Androidx网络状态检测
#### 1、依赖

```
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url("https://dl.bintray.com/xqy666666/maven")
        }
    }
}
```
```
implementation 'com.xhh.networkstate:NetworkState:1.0.1'
```
#### 2、使用

* 初始化

```
class App:Application() {

    override fun onCreate() {
        super.onCreate()
        NetworkStateManager.init(this)
    }
}
```
* 注册网络监听

```
NetworkStateManager.registerNetworkStateCallback(activity or fragment){
            isConnectd, networkType, networkName ->
            //isConnected 是否连接，networkType 网络类型 WIFI or Mobile，networkName 网络的名称

        }
```
