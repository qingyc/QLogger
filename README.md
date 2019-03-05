# GuideTipView
A Simple Android Logger


# How To Use

Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

	dependencies {
	         api 'com.github.qingyc:QLogger:0.2'
	}

Step 3. Use
 ```
  //open logger
  QLogger.init(BuildConfig.DEBUG)
  //use
  QLogger.e("xxxxxxxxxxxx")
 ```

简化自 https://github.com/orhanobut/logger