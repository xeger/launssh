#!/bin/bash

`which apt-get`
if [ $? == 0 ]; then
    apt-get install -y ruby rubygems rake epos irb git-core ruby1.8-dev build-essential libopenssl-ruby1.8
    installed_packages=1
fi

if [ "$installed_packages" != 1 ]; then
    echo "Couldn't determine your packaging system, which means I couldn't automatically"
    echo "install packages. We'll try to install RightScale anyway, but you should expect"
    echo "things to break. Good luck; you're gonna need it!"
    read -p "Press a key to continue..." useless
fi

#use curl to download tarball from S3
CODE=500
while [ &quot;$CODE&quot; -ge &quot;500&quot; ]; do
  CODE=`curl -s -S -f -L --retry 7 -w '%{http_code}' -o /tmp/blueskies.tgz http://s3.amazonaws.com/rightscale_rightlink_dev/rightscale_right_link_blueskies.tgz`
  echo &quot;Downloading $TARBALL_NAME - curl returned code: $CODE&quot;
done

#chdir to /opt and extract tarball
cd /opt
tar zxf /tmp/blueskies.tgz

#run the BlueSkies post-install script
chmod a+x /opt/rightscale/bin/post_install_nocloud.sh
exec /opt/rightscale/bin/post_install_nocloud.sh
