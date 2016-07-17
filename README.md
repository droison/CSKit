# 自定义Android开源库CSKit介绍
整个项目框架包含单例管理(MManagerCenter)，网络请求及文件下载(VolleyPlus)和事件总线管理(MMBus)的一个集合框架。还处于检验阶段，使用方法（Android studio）：

在Project的gradle文件中增加：

```xml
repositories {
	maven {
	url  "http://dl.bintray.com/droison/maven"
	}
}
```
在使用的module增加：

```xml
compile 'xyz.chaisong.cskit:cskit:0.0.3'
```

	lib编译并上传到jcenter的命令：
	$: cd cskit
	$: ../gradlew install
	$: ../gradlew bintrayUpload

项目的Github地址为<http://github.com/droison/CSKit>
## 1、MManagerCenter 
主要用于针对整个App的单例管理，已经在[好奇心日报][好奇心Android]上线使用1年多，稳定性和可用性经过检验。

基于Android的一个单例工厂，源于前公司wechat iOS版本的MManagerCenter的思想，进行Android领域的实现。

整个项目线程安全。

一般单例用于Android中的manager或者controller角色，本MManagerCenter会在程序前后台切换，程序退出以及内存警告等情况下通知维护的manager

### 快速上手
* 1、在App的Application中加入
	 `MManagerCenter.init().configDebug(isDebugMode)`
* 2、将需要单例管理的class继承MManager(推荐)或实现MManagerInterface
* 3、单例程序的初始化方法需要设定为public
* 4、使用以下代码获取单例  
	`MMServiceCenter.getService(PraiseDAO.class)`
	
### 进阶使用
* 在application的ActivityLifecycleCallbacks中进行后台和前台切换的识别，并在对应位置分别调用MMServiceCenter.callEnterBackground()和MManagerCenter.callEnterBackground()。
* 在程序退出的时候调用MManagerCenter.callTerminate()。
* 在程序中用户登陆和退出的时候分别调用MManagerCenter.callReloadData()和
MManagerCenter.callClearData()。
* 如果对已存在的Manager不满意，可以通过MManagerCenter.removeManager(Manager.class)的方式进行移除。

### 详细介绍
各种配置都在`MManagerInterface`进行了标记：

* isManagerRemoved：默认为false，当为true表示已经被MManagerCenter移除
* isManagerPersistent：默认为false，当为ture表示程序在退出时不销毁该Manager，属于那种生命周期和Application相同的。
* void onManagerInit(Context context)：Manager初始化会调用，做一些需要Context的初始化操作，建议所有的初始化都放在这里，取代默认的构造方法初始化。传入的context为ApplicationContext。
* void onManagerEnterBackground()：进入后台调用。需要app在进入后台的时候调用MManagerCenter.callEnterBackground()。
* void onManagerEnterForeground()：进入前台调用。需要app在进入前台的时候调用MManagerCenter.callEnterForeground()。
* void onManagerTerminate()：程序退出调用。需要app在退出的时候调用MManagerCenter.callTerminate()。
* void onManagerClearData()：账号退出时调用，清理当前与用户相关的数据。需要app在用户退出的时候调用MManagerCenter.callClearData()。
* void onManagerReloadData()：完成切换帐号后调用，一般用于重新load用户相关的数据。需要app在用户完成登陆的时候调用MManagerCenter.callReloadData()。

## 2、VolleyPlus
项目命名为Volley Plus，是基于谷歌开源的网络请求框架Volley的一些扩展支持，完美兼容现有Volley的全部接口和使用方法。基于的Volley版本为20151025。

现有扩展包括：

* 1、在原有Volley架构基础上增加对文件下载的支持。该文件下载不走默认的volley缓存，也不会绑定cache的header，只通过判断存储位置是否存在该文件，默认认为一个url唯一对应一个文件。文件下载会对回调下载进度，可以取消下载。该下载支持断点续传。
* 2、对Request增加Delivery属性，可以通过设置该属性自定义该Request的回调线程。原框架的回调线程为RequestQueue初始化时传入的参数，若该参数不传，即为默认主线程。

需求起源于好奇心日报[好奇心Android]中除了一般网络请求外，需要进行wifi条件的文章预加载，预加载过程需要在请求文章详情后根据返回值再预加载该文章的js和css文件，因此引入文件下载；本着尽可能避免ui线程的计算（这种预加载很可能在短时间大量回调主线程），将主线程并不需要的request回调到约定的线程。

项目在好奇心日报[好奇心Android]的3.0线上版本中使用，并持续维护中。

### 使用文件下载
初始化一个FileDownloadManager：

```java
FileDownloadManager(File rootDirectory, RequestQueue queue, ResponseDelivery defaultDelivery, int parallelTaskCount);
```
* 该manager最好在程序中用单例进行管理。
* rootDirectory表示文件下载的保存目录，该名称在启动新下载时未传入保存文件的file时有效。
* queue即volley的RequestQueue。
* 传入的delivery表示该下载器中所有回调都在delivery所在线程。
* parallelTaskCount为可以并行进行的下载，要求该值不得大于传入queue的并行下载线程数。

启动一个下载：

* add(String url, FileDownloadListener listener)。这种情况会保存在rootDirectory目录下，文件名为url的md5值
* add(File storeFile, String url, FileDownloadListener listener)。表示下载完成后会保存到storeFile位置。
        
### 使用自定义Prefetcher线程的Delivery

```java
PrefetcherThread  prefetcherThread = new PrefetcherThread("PrefetcherThread");
prefetcherThread.start();
ResponseDelivery prefetcherDelivery = new ExecutorDelivery(new Handler(prefetcherThread.getLooper(), prefetcherThread));
StringRequest request = new StringRequest(Request.Method.GET, null, null);
request.setDelivery(prefetcherDelivery);

private static class PrefetcherThread extends HandlerThread implements Handler.Callback {
	public PrefetcherThread(String name) {
 		super(name);
   	}

   	@Override
   	public boolean handleMessage(Message msg) {
		System.out.println("handleMessage CurrentThread = " + Thread.currentThread().getName());
       return true;
   	}
}
```

### 未来希望增加的扩展
* 1、扩展DiskBasedCache，增加对file下载中缓存的识别的支持，用volley已有的机制来避免已缓存却重复下载的问题，以及并行同url下载单一请求的优化。通过识别该file存储位置是否存在文件来判断是否缓存过；在启动下载时通过传入refreshNeeded 来确认在有缓存的时候是否需要重新下载，默认为false。
* 2、默认文件缓存目录的数据文件上限设置，超过上限做清理。文件下载不做过期配置，本地数据管理参考iOS框架SDWebImage的本地缓存管理机制，在超过上限的情况下删除最远下载的文件。
* 3、https的支持。

## 3、MMBus
一个事件总线，主要参考otto的事件总线管理，默认工作在主线程，区别在于事件发送更加精准，针对interface进行广播，而不是过去传入参数方式，可以有效的利用编译器的警告和高亮进行代码编写。

当前不完善，未在实际项目中检验。

[好奇心Android]:http://www.wandoujia.com/apps/com.qdaily.ui