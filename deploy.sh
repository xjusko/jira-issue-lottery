# Tracker issue for quarkus bug. This script serves
# as a workaround for the time being.
# issue: https://github.com/quarkusio/quarkus/issues/38880
# Please delete this script once the fix is released
./mvnw clean package -Dquarkus.kubernetes.deploy=true
sed -i '/^\s*selector: {}/d' target/kubernetes/openshift.yml
oc apply -f target/kubernetes/openshift.yml
