find ~/.gradle/caches/ -iname "*.jar" -exec zip -d '{}' 'META-INF/NOTICE*' \;
find ~/.gradle/caches/ -iname "*.jar" -exec zip -d '{}' 'META-INF/LICENSE*' \;
