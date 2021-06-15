FROM gcr.io/cloud-builders/javac:8

RUN apt-get update
RUN apt-get install -y curl
RUN apt-get install -y ninja-build
RUN apt-get install -y python3-pip
RUN pip3 install meson
RUN curl -o ndk.zip \
    https://dl.google.com/android/repository/android-ndk-r21e-linux-x86_64.zip
RUN unzip ndk.zip
RUN mv android-ndk-r21e /ndk

WORKDIR /src
ENTRYPOINT ["./gradlew"]
CMD ["-PndkPath=/ndk", "release"]
