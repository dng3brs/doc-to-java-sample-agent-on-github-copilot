# wsl --install --no-distribution
# wsl --update --pre-release

# wslc build --no-cache -t debianc:latest .
# wslc run --rm -it -v .:/workspaces debianc bash

# wslc system session terminate

FROM debian:13-slim

ENV LANG=C.UTF-8

RUN apt-get update && apt-get upgrade -y && apt-get install -y --no-install-recommends curl git ca-certificates gnupg sudo wget lsb-release unzip jq vim \
    && bash -c 'echo >> ~/.bashrc' \
    && bash -c 'echo "export LS_OPTIONS=\"--color=auto\"" >> ~/.bashrc' \
    && bash -c 'echo "alias ls=\"ls \$LS_OPTIONS\"" >> ~/.bashrc' \
    && bash -c 'echo "alias ll=\"ls \$LS_OPTIONS -l\"" >> ~/.bashrc' \
    && bash -c 'echo "export PS1=\"\\[\\e[01;31m\\]\\u@\\h\\[\\e[00m\\]:\\[\\e[01;34m\\]\\w\\[\\e[00m\\]\\$ \"" >> ~/.bashrc' \
    && bash -c 'echo "export PATH=\"\$PATH:~/.local/bin\"" >> ~/.bashrc' \
    && git config --global init.defaultBranch main \
    && git config --global user.email dng3brs@gmail.com \
    && git config --global user.name dng3brs \
    && git config --global core.fileMode false \
    && mkdir -p /etc/apt/keyrings \
    && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_24.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list \
    && apt-get update && apt-get install -y --no-install-recommends nodejs \
    && apt install -y --no-install-recommends python3-all python-is-python3 pipx \
    && pipx install uv \
    && apt-get install -y --no-install-recommends openjdk-21-jdk maven \
    && apt-get install -y --no-install-recommends rust-all \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspaces
