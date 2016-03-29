# MMServiceCenter

	基于Android的一个单例工厂，源于前公司iOS版本的MMServiceCenter的思想，不过此MM非彼MM，此MM为木淼之意

项目可以在多线程下工作，进行单例的管理。

一般单例用于Android中的manager或者controller角色，本MMServiceCenter会在程序前后台切换，程序退出以及内存警告等情况下通知维护的manager

## 简单使用
* 1、在工程的Application中加入

	 `MMServiceCenter.init().configDebug(isDebugMode)`
	 
* 2、将需要单例管理的class extends MMService 或者 implements MMServiceInterface

* 3、单例程序的初始化方法需要设定为public
* 4、使用以下代码获取单例  
	`MMServiceCenter.getService(PraiseDAO.class)`
* 5、其它具体使用方式请详见api，过一阶段会上传一个demo用于简单介绍使用
