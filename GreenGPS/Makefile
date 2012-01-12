# Please use the correct SDK directory, as set in local.properties
# Also, make sure ant is installed and is on your $PATH variable

all:
	ant debug
	
# Update ant files, create them if they do not exist
setup:
	android update project --path . --target android-8
	
clean:
	ant clean || true
	rm -f build.xml proguard.cfg *.properties
