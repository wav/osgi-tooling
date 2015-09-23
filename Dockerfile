FROM l3iggs/archlinux

RUN pacman -Sy --noconfirm jdk8-openjdk maven sbt git

# the directory to clone into.
RUN mkdir /root/input /root/output

## These are mapped for 2 purposes, caching and collecting results
## from publishing locally.
VOLUME ["/root/input", "/root/.m2", "/root/.ivy2", "/root/output", "/root/.sbt/boot"]