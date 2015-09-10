FROM wav/karaf-minimal:4.0.1

ENV SECRETS /secrets

VOLUME ["/secrets"]

ADD prep.sh /

RUN chmod +x /prep.sh

RUN mkdir /prep

ONBUILD ADD . /prep/

ONBUILD RUN /prep.sh