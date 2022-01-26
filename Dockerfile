FROM ubuntu:20.04 AS builder

RUN apt-get update && \
    apt-get install -yqq wget build-essential libz-dev zlib1g-dev apt-transport-https curl gnupg

RUN wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.0.0.2/graalvm-ce-java11-linux-amd64-22.0.0.2.tar.gz
RUN tar zxvf graalvm-ce-java11-linux-amd64-22.0.0.2.tar.gz && \
    mkdir -v /usr/lib/jvm && \
    mv -v graalvm-ce-java11-22.0.0.2/ /usr/lib/jvm && \
    update-alternatives --install /usr/bin/java java /usr/lib/jvm/graalvm-ce-java11-22.0.0.2/bin/java 0 && \
    /usr/lib/jvm/graalvm-ce-java11-22.0.0.2/bin/gu install native-image && \
    ln -sv /usr/lib/jvm/graalvm-ce-java11-22.0.0.2/bin/native-image /usr/local/bin/native-image

RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import && \
    chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg && \
    apt-get update && \
    apt-get install -y sbt

COPY . /build
WORKDIR /build
RUN sbt graalvm-native-image:packageBin

FROM ubuntu:20.04

RUN mkdir -v /app
WORKDIR /app
COPY --from=builder /build/target/graalvm-native-image/xurl xurl
CMD ./xurl
