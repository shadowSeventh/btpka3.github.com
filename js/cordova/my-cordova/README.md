


```
npm install -g cordova 
npm update -g cordova

cordova create my-app 
cd my-app
sudo ln -s /usr/bin/chromium-browser /usr/bin/google-chrome
cordova platform add --save browser 
cordova platform add --save android
cordova platform add --save ios 
cordova run browser
 
 
# cordova run android
cordova emulate android --target test1

cordova plugin add --save cordova-plugin-camera
cordova plugin add --save cordova-hot-code-push-local-dev-addon

#cordova plugin add --save cordova-plugin-app-update
cordova plugin add --save cordova-plugin-dynamic-update


#.gitignore `platforms` and `plugins` directory
# 以下命令会根据 config.xml 自动创建出以上两个目录
cordova prepare

cordova build --release android


# udpate plugins
cordova plugin list
npm install -g cordova-check-plugins
cordova-check-plugins --update=auto

```

# 真机测试

```
# 查看有哪些手机列表
adb devices

# 真机运行 
cordova run android --device
adb logcat |grep btpka3
```


# 一些插件

```
# 微信分享、支付
cordova plugin add cordova-plugin-tx-wechat --variable APP_ID=[你的APPID]

cordova-plugin-splashscreen
cordova-plugin-network-information
cordova-plugin-file
cordova-plugin-geolocation
cordova-sqlite-storage
cordova-plugin-statusbar
cordova-plugin-calendar
ionic-plugin-keyboard
cordova-plugin-crop


cordova-plugin-file-transfer

phonegap-plugin-contentsync
cordova-hot-code-push-plugin
cordova-hot-code-push-cli
cordova-plugin-code-push
cordova-plugin-browsersync

cordova-plugin-camera
cordova-plugin-inappbrowser
cordova-plugin-contacts
cordova-plugin-media
cordova-plugin-media-capture
cordova-plugin-dialogs

cordova-plugin-baidupush
```

# apk签名

```
keytool -genkeypair \
    -alias btpka3 \
    -keyalg RSA \
    -keysize 1024 \
    -sigalg SHA1withRSA \
    -dname "CN=btpka3.github.io, OU=R&D, O=\"WeRun Club\", L=WeiHai, S=ShanDong, C=CN" \
    -validity 3650 \
    -keypass 123456 \
    -keystore sos.jks \
    -storepass 123456

# 签名
jarsigner -keystore sos.jks \
    -storepass 123456 \
    -storetype jks \
    -keypass 123456 \
    -digestalg SHA1 \
    -sigalg SHA1withRSA \
    -tsa http://timestamp.digicert.com \
    -signedjar platforms/android/build/outputs/apk/android-release-signed.apk \
    platforms/android/build/outputs/apk/android-release-unsigned.apk \
    btpka3



# 验证
jarsigner -verify \
    -keystore sos.jks \
    -storetype jks \
    -tsa http://timestamp.digicert.com \
    -digestalg SHA1 \
    -sigalg SHA1withRSA \
    platforms/android/build/outputs/apk/android-release-signed.apk

```

# 动态更新

应当考虑使用 cordova-plugin-code-push, 

```
code-push release <appName> path_to_your_app/platforms/android/assets/www <appStoreVersion> --deploymentName <AndroidDeploymentName>
```

# .gitignore

```
.gradle
build
node_modules
bower_components

# www目录由前端工程打包过来
www
platforms/android/assets/www
platforms/ios/www
```


# <del>开发时热部署</del>

```
cd my-cordova
cordova plugin add cordova-hot-code-push-plugin
cordova plugin add cordova-hot-code-push-local-dev-addon
npm install -g cordova-hot-code-push-cli
cordova-hcp init
# 之后,修改成成 cordova-hcp.json

cordova-hcp server


cordova run android --device
```
