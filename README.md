# jsbridge v2.0

CN: https://blog.csdn.net/jzlhll123/article/details/136345478
继承自：
https://github.com/lzyzsd/JsBridge 1.0.4的版本还是挺好用的。最新的master，
他并没有打包成aar公开，而且master代码已经被各种提交污染导致bug。 
使用的是：shouldOverrideUrlLoading + Iframe.src更新的相互通知的机制。那么为了兼容android4.x，必须采用1.0.4版本。

那么我基于该框架，也是为了完全兼容以前的前端H5代码，实现了jsbridge2.0。目前公司日活10w+。
支持androiod5.0+，使用evaluateJavascript和重写，代码上也能简化交互逻辑。
解决1.0.4和master中的问题：jsbridge2.0 解决了大量存在的问题。

demo参考：
https://github.com/jzlhll/AndroidComponts 主工程。
com.allan.androidlearning.activities.WebEcharts****几个Fragment，和assets里面的html
getuserinfo.html和WebBridgeFragment.kt



## onPageFinished插入js片段问题
坑点：某些网址无法触发。
包括他现在的代码都是在onPageFinished以后才loadJs文件，这个时机对于某些在线网址，可能由于加载过慢或者有block的情况，无法触发。改成onPageStarted 后500ms或者onPageFinished立刻执行加载，来保证一定执行。

## 无法通信原因是iframe被清理
坑点：有的网址使用的vue框架，无法jsbridge通信，研究很久，发现iframe会被清理。
js注入片段，调整init函数之后create iframe才行。master有解决。

## JSONObject queto，转义问题
坑点：替换不全，master代码有解决。
但是注意queto函数之后，多了2个引号。如果使用evaluateJavascript则需要考虑你的函数，WebViewJavascriptBridge._handleMessageFromNative(%s); 而不是WebViewJavascriptBridge._handleMessageFromNative(‘%s’);

## 二次过快执行loadUrl导致丢失指令
坑点：通信框架出现问题或者消息丢失
因为loadUrl多次，导致_fetechQueue多次，第一次的iframe将messageQueue传递了进去，但是没有触发shouldOverride，第二次的messageQueue又是空的。导致消息丢失。master有从js的执行上做了延迟处理解决。我遇到的就是chromium版本低的情况，出现该问题。导致，初始化信息未能给到H5。
之所以原来的代码这样做是为了支持android4.x，那么， 到了今天我们的app基本上最低要求minSDK都是android6.0+，甚至8.0了。所以抛弃掉使用shouldOverride这套机制，而是直接通过evaluateJavascript来实现，这样可以避免大量的来来回回的传递消息。

## 长度过长loadUrl vs evaluateJavascript
master上有根据长度，做了处理。大约2M。原因是webView限制loadUrl的长度，而evaluateJavascript不限制。

## 不使用代理模式，包裹WebClient
master代码被提交搞的复杂了。子类想改的话，自行继承实现genereateWebClient函数。这样来降低框架复杂性。

## 效率对比
抛弃4.4的loadUrl+shouldOverrideUrlLoading +iframe更新机制。
完全采用evaluateJavascript即可。
既能简化jsbrige框架，又能提升效率。无需来来回回的向上向下通知。

## 添加了混淆规则
新写的2.0框架在，则因为使用JavascriptInterface，则需要注意混淆问题。