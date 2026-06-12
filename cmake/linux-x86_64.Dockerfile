FROM gcc:14-bookworm

RUN echo 'deb https://deb.debian.org/debian bookworm-backports main' >> /etc/apt/sources.list

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    cmake \
    ninja-build \
    nasm \
    autoconf \
    automake \
    autopoint \
    autotools-dev \
    openjdk-17-jdk \
    texinfo \
    gettext \
    libvulkan-dev \
    python3-pip \
    python3-packaging \
    pipx \
    libwebkit2gtk-4.1-dev \
    libxrandr-dev \
    libxinerama-dev \
    libxcursor-dev \
    mesa-common-dev \
    libx11-xcb-dev \
    -t bookworm-backports

RUN pip install meson --break-system-packages

RUN curl -Lo node.tar.gz https://nodejs.org/dist/v24.8.0/node-v24.8.0-linux-x64.tar.gz \
      && echo "daf68404b478b4c3616666580d02500a24148c0f439e4d0134d65ce70e90e655 node.tar.gz" | sha256sum -c - \
      && tar xzf node.tar.gz --strip-components=1 -C /usr/local/

RUN mkdir /.npm && chown -R 1000:1000 /.npm

USER 1000:1000
WORKDIR build

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/

ENTRYPOINT ["./cmake/linux-x86_64-build.sh"]
