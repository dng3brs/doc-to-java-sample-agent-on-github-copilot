# wsl --install --no-distribution
# wsl --update --pre-release

# wslc build --no-cache -t debianc:latest .
# wslc run --rm -it -v .:/workspaces debianc bash

# wslc system session terminate

FROM debian:13-slim

# ARG USERNAME=foo
# ARG USER_UID=1000
# ARG USER_GID=$USER_UID

ENV LANG=C.UTF-8

RUN apt -y -U upgrade && \
    apt -y install --no-install-recommends curl git ca-certificates gnupg sudo wget lsb-release \
#    && groupadd --gid $USER_GID $USERNAME \
#    && adduser --uid $USER_UID --gid $USER_GID --disabled-password --gecos "" --shell /bin/bash $USERNAME \
#    && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
#    && chmod 0440 /etc/sudoers.d/$USERNAME \
    && git config --global user.name 'dng3brs' \
    && git config --global user.email 'dng3brs@gmail.com' \
    && git config --global init.defaultBranch main \
#    && git config --global core.fileMode false \
    && apt-get clean 

# USER $USERNAME
WORKDIR /workspaces