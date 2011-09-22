rem em ber  to  replace this path to your Protege directory
pushd d:\Program Files\Protege_4.1
java -Dlog4j.configuration=file:log4j.xml -DentityExpansionLimit=100000000 -Dfile.encoding=utf-8 -Dorg.protege.plugin.dir=plugins -classpath bin/felix.jar;bin/ProtegeLauncher.jar org.protege.osgi.framework.Launcher  %*
popd