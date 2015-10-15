docker run --name karaf -d -p 1099:1099 -p 8181:8181 -p 8081:8081 wav/karaf:4.0.2-minimal && \
# sleep 3 && docker exec -it karaf /opt/karaf/bin/status
# docker exec -it karaf /opt/karaf/bin/client