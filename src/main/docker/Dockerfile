FROM nexus3.onap.org:10001/onap/integration-java11:10.0.0

ENV HOSTALIASES /etc/host.aliases

ARG user=restconfcollector
ARG group=restconfcollector

USER root
WORKDIR /opt/app/restconfcollector

RUN apk --no-cache update && apk --no-cache upgrade && apk add --upgrade procps && apk add --no-cache vim && apk --no-cache add curl && \
    addgroup $group && adduser --system --disabled-password --no-create-home --ingroup $group $user && \
    chown -R $user:$group /opt/app/restconfcollector && \
    chmod -R u+rw /opt/app/restconfcollector

USER  $user

COPY --chown=$user:$group /. /opt/app/restconfcollector/

RUN chmod +x bin/docker_entry.sh
RUN chmod +x bin/restConfCollector.sh

EXPOSE 8080 8687

ENTRYPOINT ["bin/docker_entry.sh"]
