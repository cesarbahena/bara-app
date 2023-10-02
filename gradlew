#!/usr/bin/env bash

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You may also use JAVA_OPTS and GRADLE_OPTS.
# The user can override these options when running Gradle.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Use the maximum available of file descriptors for the current process
# See https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html#options-for-troubleshooting
# This needs to be set before any subprocess is launched.
# The value is "unlimited" on macOS and most Linux distributions.
# This should not be used on systems where "unlimited" is not supported,
# which is generally very old OS versions.
MAX_FD_LIMIT=$(ulimit -Hn 2>/dev/null)
if [ $? -eq 0 ]; then
    ulimit -n ${MAX_FD_LIMIT}
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses "$JAVA_HOME/jre/sh/java" as the actual executable.
        JAVA_EXE="$JAVA_HOME/jre/sh/java"
    elif [ -x "$JAVA_HOME/bin/java" ] ; then
        JAVA_EXE="$JAVA_HOME/bin/java"
    else
        echo "WARNING: JAVA_HOME is set but $JAVA_HOME/bin/java does not exist."
        echo "         Proceeding with command 'java'."
        JAVA_EXE="java"
    fi
else
    JAVA_EXE="java"
fi

# OS specific support (must be 'true' or 'false').
cygwin=false
darwin=false
mingw=false
case "`uname`" in
  CYGWIN*)
    cygwin=true
    ;;
  Darwin*)
    darwin=true
    ;;
  MINGW*)
    mingw=true
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything else
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# Attempt to set APP_HOME
# Resolve links
PRG="$0"
while [ -h "$PRG" ]; do
    LS="-ls"
    if [ "$darwin" = "true" ]; then
        LS="-lsd"
    fi
    link=`$LS "$PRG"`
    (set -x; expr "$link" : '.*->\ \(.*\)$' > /dev/null)
    link=`expr "$link" : '.*->\ \(.*\)$' || true`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done
APP_HOME=`dirname "$PRG"`
APP_HOME=`cd "$APP_HOME" && pwd`

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# Set GRADLE_HOME to APP_HOME
GRADLE_HOME="$APP_HOME"

# Set GRADLE_USER_HOME if not already set
if [ -z "$GRADLE_USER_HOME" ] ; then
    if [ -d "$HOME/.gradle" ] ; then
        GRADLE_USER_HOME="$HOME/.gradle"
    else
        GRADLE_USER_HOME="$APP_HOME/.gradle"
    fi
fi

# Add default JVM options here. You may also use JAVA_OPTS and GRADLE_OPTS.
# The user can override these options when running Gradle.
if [ -z "$DEFAULT_JVM_OPTS" ] ; then
    DEFAULT_JVM_OPTS=""
fi

# Collect all arguments for the Java command
JAVA_ARGS=()
if [ -n "$DEFAULT_JVM_OPTS" ] ; then
    JAVA_ARGS+=($DEFAULT_JVM_OPTS)
fi

# Add any custom JVM options from GRADLE_OPTS or JAVA_OPTS
if [ -n "$GRADLE_OPTS" ] ; then
    JAVA_ARGS+=($GRADLE_OPTS)
fi
if [ -n "$JAVA_OPTS" ] ; then
    JAVA_ARGS+=($JAVA_OPTS)
fi

# Add the main class
JAVA_ARGS+=(-cp "$APP_HOME/gradle/wrapper/gradle-wrapper.jar")
JAVA_ARGS+=("org.gradle.wrapper.GradleWrapperMain")

# Add Gradle command line arguments
for arg in "$@"; do
    JAVA_ARGS+=("$arg")
done

# Execute Java command
exec "$JAVA_EXE" "${JAVA_ARGS[@]}"