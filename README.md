## 前置き
AndroidのRAM、CPU、GPUの監視ツールです  
Win環境でAndroid Studio無しで作りました  
WSL2内コンテナでAVDを起動し、ホストのXlaunchに描画します

## このレポジトリをクローンしたらやること
1. **Xlaunchのセットアップ**  
以下から入手できます  
https://sourceforge.net/projects/vcxsrv/  
Display numberに0、Disable access controlにチェックを入れ「完了」をクリックしてください

2. **コンテナ起動**  
```
wsl

HOST_IP=$(ip route | grep default | awk '{print $3}') docker-compose up -d

docker exec -it dev_emulator bash
```

3. **gradle wrapper 生成**  
```
gradle wrapper --gradle-version 8.7 --no-daemon
```

4. **AVD起動**  
```
emulator -avd dev_avd -qemu -enable-kvm
```
※ここでホストのXlaunchにエミュレータが描画されることを確認してください

5. **APK をビルド&インストール**  
```
./gradlew assembleDebug

adb install app/build/outputs/apk/debug/app-debug.apk
```
※またはgradlew
```
./gradlew installDebug --continuous
```

## プロジェクト構成
```
android_resource_monitor/
└── app/
    ├── src/
    │   └── main/
    │       ├── java/com/resource_monitor/
    │       │   ├── GLRenderer.kt               # OpenGL レンダラー
    │       │   ├── MainActivity.kt             # メイン画面
    │       │   └── ui/
    │       │       └── theme/
    │       │           ├── Color.kt            # カラーパレット
    │       │           ├── Theme.kt            # テーマ定義
    │       │           └── Type.kt             # Typography定義
    │       ├── res/
    │       │   └── values/
    │       │       ├── strings.xml             # 文字列リソース
    │       │       └── themes.xml              # テーマリソース
    │       └── AndroidManifest.xml             # マニフェストファイル
    ├── build.gradle.kts                        # モジュールビルド設定
    └── proguard-rules.pro                      # ProGuard設定
```
