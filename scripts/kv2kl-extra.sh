#
# If the environment variables NAME 
# is set they are used. If not an PIDFILE variable
# is generated in this script.
#

#BINDIR="$app_home"
BINDIR="$(dirname $0)"
PREFIX="$(dirname $BINDIR)"

if [ "x$KVDIST" = "x"  ]; then
	if [ "$BINDIR" = "/usr/bin" ]; then
		KVDIST="/"
	else
		KVDIST="$PREFIX"
	fi
fi

declare -r KVDIST="$KVDIST"
declare -r ETCDIR="$KVDIST/etc/kvalobs"
declare -r LIBDIR="$KVDIST/var/lib/kvalobs"
declare -r RUNDIR="$KVDIST/var/lib/kvalobs/run"
declare -r LOGDIR="$KVDIST/var/log/kvalobs"
declare -r KVAPP_NAME="$(basename "$0")"
declare -r USEPID="$$"

echo "BINDIR=${BINDIR}"
echo "KVDIST=${KVDIST}"
echo "KVAPP_NAME=${KVAPP_NAME}"
echo "RUNDIR=${RUNDIR}"
echo "ETCDIR=${ETCDIR}"
echo "LOGDIR=${LOGDIR}"
echo "LIBDIR=${LIBDIR}"
echo "USEPID=${USEPID}"


addJava "-DKVDIST=${KVDIST}"
addJava "-DRUNDIR=${RUNDIR}"
addJava "-DETCDIR=${ETCDIR}"
addJava "-DLOGDIR=${LOGDIR}"
addJava "-DLIBDIR=${LIBDIR}"
addJava "-DKVAPP_NAME=${KVAPP_NAME}"
addJava "-DUSEPID=${USEPID}"


