FROM anapsix/alpine-java:8

ADD karaf /opt/karaf

    #  SSH  ADMIN RMI
EXPOSE 8101 8181  1099

RUN chmod +x \
  /opt/karaf/bin/karaf \
  /opt/karaf/bin/status \
  /opt/karaf/bin/client

WORKDIR /opt/karaf

ENTRYPOINT ["/opt/karaf/bin/karaf", "server", "clean"]