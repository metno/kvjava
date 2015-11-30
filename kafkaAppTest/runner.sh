#! /bin/sh

cwd=`pwd`
depdir="$cwd/target/dependency"

if [ ! -d $depdir ]; then
	mvn dependency:copy-dependencies
	
	if [ ! -d $depdir ]; then
		echo "Could not download depencies to '$depdir'"
		exit 1
	fi	
fi

CLASSPATH=""

for file in `ls -1 $depdir`; do
	CLASSPATH="$CLASSPATH:./target/dependency/$file"
done

echo $CLASSPATH

export CLASSPATH

java -jar $1




