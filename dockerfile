#
# BUILD BASE IMAGE (USED ONLY Windows11 LOCAL)
# ------------------------------------------------------------------------------
FROM debian:bullseye

# set DISPLAY environment variable for X11 forwarding
ARG HOST_IP
ENV DISPLAY=${HOST_IP}:0

# install dependent packages
RUN apt-get -y update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    openjdk-17-jdk \
    lib32stdc++6 \
    lib32z1 \
    libx11-6 \
    libxcb1 \
    libxkbcommon-x11-0 \
    libxcb-cursor0 \
    libxcb-xinerama0 \
    libxcb-icccm4 \
    libxcb-keysyms1 \
    libxcb-image0 \
    libxcb-util1 \
    libxcb-randr0 \
    libxcb-shape0 \
    libxcb-xkb-dev \
    libxkbcommon-x11-dev \
    libsm6 \
    libice6 \
    libc++1 \
    libc++-dev \
    libfontconfig1 \
    libgl1-mesa-glx \
    libxkbfile1 \
    libpulse0 \
    mesa-utils \
    g++ \
    build-essential \
    qtbase5-dev \
    qttools5-dev-tools \
    && apt install git bridge-utils -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# install Android SDK
RUN curl -o sdk-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-8092744_latest.zip \
    && mkdir -p /opt/android-sdk/cmdline-tools/latest \
    && unzip sdk-tools.zip -d /opt/android-sdk/cmdline-tools/latest \
    && mv /opt/android-sdk/cmdline-tools/latest/cmdline-tools/* /opt/android-sdk/cmdline-tools/latest/ \
    && rmdir /opt/android-sdk/cmdline-tools/latest/cmdline-tools \
    && rm sdk-tools.zip
# sdkmanagerは/cmdline-tools/latestの下にbinの配置を期待してる
# が、unzipで展開するとcmdline-toolsの直下にbinが展開されてしまう為、latestにmvする

ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"

# install Android SDK components
RUN yes | sdkmanager --sdk_root=$ANDROID_SDK_ROOT \
    "platform-tools" \
    "platforms;android-34" \
    "platforms;android-35" \
    "platforms;android-36" \
    "build-tools;34.0.0" \
    "build-tools;35.0.0" \
    "build-tools;36.0.0" \
    "ndk;27.3.13750724" \
    "cmake;3.22.1" \
    "system-images;android-36;google_apis;x86_64"

# install Kotlin
RUN curl -L -o kotlin.zip https://github.com/JetBrains/kotlin/releases/download/v1.9.24/kotlin-compiler-1.9.24.zip \
    && mkdir -p /opt/kotlin \
    && unzip -q kotlin.zip -d /opt/kotlin \
    && mv /opt/kotlin/kotlinc /opt/kotlin/latest \
    && rm kotlin.zip
ENV PATH="/opt/kotlin/latest/bin:$PATH"

# install Gradle
RUN curl -L -o gradle.zip https://services.gradle.org/distributions/gradle-8.7-bin.zip \
    && mkdir -p /opt/gradle \
    && unzip -q gradle.zip -d /opt/gradle \
    && mv /opt/gradle/gradle-8.7 /opt/gradle/latest \
    && rm gradle.zip
ENV GRADLE_HOME=/opt/gradle/latest
ENV PATH="$GRADLE_HOME/bin:$PATH"

# specify working directory
WORKDIR /emulator

# create emulator
RUN avdmanager create avd -n dev_avd -k "system-images;android-36;google_apis;x86_64" -d "pixel_5"
CMD ["tail", "-f", "/dev/null"]
