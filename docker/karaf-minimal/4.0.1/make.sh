. util.sh

rm -Rf ./.out ./karaf && mkdir -p ./.out

mvnGet org.apache.karaf:apache-karaf-minimal:4.0.1:tar.gz karaf.tar.gz || exit 1

tarFromOutDir karaf.tar.gz karaf || exit 1

buildImage