FROM clojure:temurin-25-tools-deps-1.12.3.1577-noble AS base

RUN apt-get update \
    && apt-get upgrade -y --with-new-pkgs \
    && apt-get install -y locales tzdata \
    && cp /usr/share/zoneinfo/UTC /etc/localtime || true

ENV LANG=C.UTF-8
RUN echo "LC_ALL=en_US.UTF-8" >> /etc/environment
RUN echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen
RUN echo "LANG=en_US.UTF-8" > /etc/locale.conf
RUN locale-gen en_US.UTF-8

FROM base AS dev

RUN apt-get install -y git

RUN curl -fsSL https://deb.nodesource.com/setup_22.x -o nodesource_setup.sh
RUN bash nodesource_setup.sh
RUN apt-get install -y nodejs
RUN npm install --global yarn
