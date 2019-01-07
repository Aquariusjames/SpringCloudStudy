# cetos7网络设置
MTU通信术语 最大传输单元（Maximum Transmission Unit，MTU）是指一种通信协议的某一层上面所能通过的最大数据包大小（以字节为单位）。最大传输单元这个参数通常与通信接口有关
cat /sys/class/net/eth0/mtu （centos6） 查看MTU值 
cat /sys/class/net/ens33/mtu  （centos7） 查看MTU值 
重启网络失败解决 解决命令：
systemctl stop NetworkManager
systemctl disable NetworkManager
重新启动网络：
systemctl start network.service
# centos7 ping www.baidu.com ping 不通。
vi /etc/resolv.conf 添加 nameserver 8.8.8.8
                         search localdomain
ifcfg-ethxx DNS=8.8.8.8
# centos6克隆后的系统和原系统MAC地址和UUID一样，删除UUID和MAC地址
vi /etc/sysconfig/network-scripts/ifcfg-eth0
删除网卡相关信息的文件
cd /etc/udev/rules.d
rm -f 70-presistent-net.rules
重启系统后，再重启网卡即可
reboot
service network restart


#centos7.6网络配置
TYPE=Ethernet
PROXY_METHOD=none
BROWSER_ONLY=no
BOOTPROTO=static
DEFROUTE=yes
IPV4_FAILURE_FATAL=no
IPV6INIT=yes
IPV6_AUTOCONF=yes
IPV6_DEFROUTE=yes
IPV6_FAILURE_FATAL=no
IPV6_ADDR_GEN_MODE=stable-privacy
NAME=ens33
UUID=b75fb46b-0ec5-48ac-acec-20faedbc71b1
DEVICE=ens33
ONBOOT=yes
IPADDR=192.168.84.23
NETMASK=255.255.255.0
NM_CONTROLLED=no
GATEWAY=192.168.84.11