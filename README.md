# jsbridge v2.0 Chinese

[![](https://jitpack.io/v/jzlhll/jsbridgeV2.svg)](https://jitpack.io/#jzlhll/jsbridgeV2)

```groovy
implementation 'com.github.jzlhll:jsbridgeV2:version'
```

https://github.com/lzyzsd/JsBridge 1.0.4的版本还是挺好用的。为了兼容android4.x而设计的。

> 它的原理**loadUrl** + **shouldOverrideUrlLoading + Iframe.src**更新的相互通知的机制。
>
> 1. 准备工作做好基础的插入js片段；
> 2. 通过native java代码将消息拼接到一段执行handleMsgDisptach的script字符串，调用WebView的loadUrl(script)；通过这种方式实现native调用WebView js；
> 3. js掉native，则是通过iframe.src的赋值，进而触发native的shouldOverrideUrlLoading函数；native可以解析得到消息内容。
> 4. 然后设计一些缓存map，msgId，做callback实现等。

但是到了今天，可以摒弃4.x。于是我基于该框架，并完全兼容以前的前端H5代码，实现了jsbridge2.0。目前公司日活顶峰50w+。支持androiod5.0+，使用evaluateJavascript和重写，代码上也能简化。

> 我的原理webView的`addJavascriptInterface(bridgeObject)` + `evaluateJavascript` 
>
> 1. 准备工作做好基础的插入js片段；并插入addJavascriptInterface的对象体；
> 2. native调用webView很简单了，直接evaluateJavascript就可以传递内容而且没有2MB大小限制；
> 3. webView调用native也简单了，通过对象体的加了`@JavascriptInterface`的函数直接调用native即可。
> 4. 再通过消息体传递msgId，设计一点map，做callback即可。



## 优化点
> 解决原代码1.0.4和master中的问题：jsbridge2.0 解决了大量存在的问题。

* 新增scheme uri跳转支持。2024/07

* onPageFinished插入js片段问题
坑点：某些网址无法触发。
包括他现在的代码都是在onPageFinished以后才loadJs文件，这个时机对于某些在线网址，可能由于某些组件刷新机制，或者加载过慢的情况，无法触发。
改成onPageStarted 后500ms或者onPageFinished立刻执行加载，来保证一定执行。

* 无法通信原因是iframe被清理
	坑点：有的网址使用的某些框架搭建的，无法jsbridge通信，研究了很久，发现iframe会被清理。
	js注入片段，调整init执行顺序。master有解决。
	
* JSONObject queto，转义问题
	坑点：替换不全，master代码有解决。
	但是注意queto函数之后，多了2个引号。
	如果使用evaluateJavascript则需要注意，WebViewJavascriptBridge._handleMessageFromNative(%s); 而不是WebViewJavascriptBridge._handleMessageFromNative('%s'); 
	
*  二次过快执行loadUrl导致丢失指令
	坑点：通信框架出现问题或者消息丢失
	原因loadUrl多次，导致_fetechQueue多次，第一次的iframe将messageQueue传递了进去，但是没有触发shouldOverride，第二次的messageQueue又是空的。
	导致消息丢失。master有从js的执行上做了延迟处理解决。我遇到的就是chromium版本低的情况，出现该问题。导致，初始化信息未能给到WebView。
	之所以原来的代码这样做是为了支持android4.x，那么，到了今天我们的app基本上最低要求minSDK都是android6.0+，甚至8.0了。
	所以抛弃掉使用shouldOverride这套机制，而是直接通过evaluateJavascript来实现，这样可以避免大量的来来回回的传递消息。也解决此问题。
	
* 长度过长loadUrl vs evaluateJavascript
	master上有根据长度，做了处理。大约2M。原因是webView限制loadUrl的长度，而evaluateJavascript不限制。无此问题。
	
* 不使用代理模式，包裹WebClient
	原代码master代码被提交搞的复杂了。子类想改的话，自行继承实现genereateWebClient函数。这样来降低框架复杂性。
	
* 效率对比
	抛弃4.4的loadUrl+shouldOverrideUrlLoading +iframe更新机制。
	完全采用evaluateJavascript即可。
	既能简化jsbrige框架，又能提升效率。无需来来回回的向上向下通知。
	
* 添加了混淆规则
 新写的2.0框架在，则因为使用JavascriptInterface，则需要注意混淆问题。
```java
-keepattributes Annotation
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers public class com.github.lzyzsd.jsbridge.BridgeObject{
    <fields>;
    <methods>;
    public *;
    private *;
}
```

# jsbridge 2.0 English

Inherited from: https://github.com/lzyzsd/JsBridge 1.0.4 version is quite useful. The latest master, it is not packaged as aar public, and the master code has been contaminated by various commits resulting in bugs. Using: shouldOverrideUrlLoading + Iframe.src update mutual notification mechanism. Then in order to be compatible with android4.x, the 1.0.4 version must be used.

I basically rewrote it all based on the framework. At the same time, is * fully compatible * with the original jsbridge code, implementing jsbridge 2.0. 
The current company DAU 20w +. Support androiod5.0 +, use evaluateJavascript and rewrite, the code can also simplify the interaction logic and solve a lot of problems existing in the original framework.

## demo reference
https://github.com/jzlhll/AndroidComponts Main project. com.allan.androidlearning.activities. WebEcharts **** Several Fragments, and htmlgetuserinfo.html and WebBridgeFragment.kt.

## Solved issues
### 1. onPageFinished js inject problem
Some URLs cannot be triggered. 
The loadJs file is only loaded after onPageFinished. At this time, for some online URLs, it may not be triggered due to slow loading or block conditions. 
After changing to onPageStarted 500ms or onPageFinished, the load will be executed immediately to ensure certain execution.

### 2. The reason for the inability to communicate is that the iframe is cleaned
Some URLs use the vue framework, which cannot be communicated with jsbridge. After studying for a long time, I found that the iframe will be cleaned up. Js injects fragments, and creates iframes after adjusting the init function. 

### 3. JSONObject queto, escape problem
Incomplete replacement.

### 4. Executing loadUrl too fast twice causes lost instructions
The original framework will loss messages, because the message delivery is implemented through loadUrl.It is executed many times, and then `_fetechQueue` many times. 
The first iframe passes the messageQueue in, but shouldOverride is not triggered, and the second messageQueue is empty. The message is lost. 
The original framework master has delayed the execution of js to solve the problem. 
Well, today our app basically requires the minimum minSDK to be android6.0 +, or even 8.0. So abandon the use of `shouldOverride`, and directly implement it through evaluateJavascript, which can avoid a lot of issues.

### 4. loadUrl vs evaluateJavaScript
The original framework has been processed according to the length. The reason is that webView limits the length of loadUrl to about 2M, while evaluateJavascript no limit.

### 5. Without using proxy mode, just wrap WebClient
The original framework master code was submitted to make it complicated. In my code, if you want to change the subclass, you can inherit and implement the genereateWebClient function by yourself. This will reduce the difficulty of reading the framework.

### 5. Efficiency improvement
Abandon the 4.4 loadUrl+shouldOverrideUrlLoading + iframe update mechanism. instead of evaluateJavaScript. It can simplify the jsbrige framework and improve efficiency. 

## Other
### 1. Added obfuscation rule
The newly written 2.0 framework is now available, and due to the use of JavaScriptInterface, it is necessary to pay attention to the issue of confusion.
arr has already included.


### 2.Easy to use
BridgeWebChromeClient add support to realize H5 web page selection picture support; 
realize full screen playback; 
remove the black play button these regular functions. 

By using: BridgeWebViewExFragment support the above functions. And it is recommended to use FragmentContainerView to wrap BridgeWebViewExFragment use.

## 机制

总结了原来jsbridge的基本逻辑为如下三幅图：
忽略了msgQueue，初始化如何数组转变等细节。
**loadUrl**+**shouldOverrideurlLoading + Iframe.src**更新的相互通知的机制：



![jsbridge初始化](https://i-blog.csdnimg.cn/blog_migrate/3d5ae0c76da11839511600f5d55b0a73.png)
![请添加图片描述](https://i-blog.csdnimg.cn/blog_migrate/35158ee5236ebec467fdd08f80555ec9.png)
![请添加图片描述](https://i-blog.csdnimg.cn/blog_migrate/4eefa7abb6b969b86361fff3229dcbc0.png)
