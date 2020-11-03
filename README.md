# AudioRecorderView

Simple Audio Recorder View with 'swap-to-cancel' Like Telegram

<img src="art/demo.gif">

## Usage
```xml

    // AndroidManifest.xml
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    // layout include AudioRecordView
    <com.tougee.recorderview.AudioRecordView
            android:id="@+id/record_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"/>

```
``` kotlin

        // activity implemented AudioRecordView.Callback

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_main)

                record_view.activity = this
                record_view.callback = this
                record_view.setTimeoutSeconds(20)
            }

        override fun onRecordStart(audio: Boolean) {}

        override fun isReady() = true

        override fun onRecordEnd() {}

        override fun onRecordCancel() {}

```

## Setup
### Android Studio / Gradle
Add the following dependency in your root build.gradle at the end of repositories:
```Gradle
allprojects {
    repositories {
        //...
        maven { url = 'https://jitpack.io' }
    }
}
```
Add the dependency:
```Gradle
dependencies {
    implementation 'com.github.tougee:audiorecorderview:1.0.1'
}
```

## License details

```
Copyright 2018 Touge

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```