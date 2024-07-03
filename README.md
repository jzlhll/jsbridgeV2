# jsbridge v2.0

[![](https://jitpack.io/v/jzlhll/jsbridgeV2.svg)](https://jitpack.io/#jzlhll/jsbridgeV2)

```groovy
implementation 'com.github.jzlhll:jsbridgeV2:1.0.8'
```
CN: https://blog.csdn.net/jzlhll123/article/details/136345478

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

