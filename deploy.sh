./mvnw clean package -Dquarkus.kubernetes.deploy=true
sed -i '/^\s*selector: {}/d' target/kubernetes/openshift.yml
oc apply -f target/kubernetes/openshift.yml
