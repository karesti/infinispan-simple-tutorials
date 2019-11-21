docker volume rm xsite-vol-lon
docker volume rm  xsite-vol-nyc
docker volume create xsite-vol-lon
docker volume create xsite-vol-nyc
cp infinispan-xsite-LON.xml custom-udp-LON.xml /var/lib/docker/volumes/xsite-vol-lon/_data
cp infinispan-xsite-NYC.xml custom-udp-NYC.xml /var/lib/docker/volumes/xsite-vol-nyc/_data
docker-compose up