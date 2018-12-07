# cetos7网络设置
重启网络失败解决 解决命令：
systemctl stop NetworkManager
systemctl disable NetworkManager
重新启动网络：
systemctl start network.service
# centos7 ping www.baidu.com ping 不通。
vi /etc/resolv.conf 添加 nameserver 8.8.8.8
ifcfg-ethxx DNS=8.8.8.8
# centos6克隆后的系统和原系统MAC地址和UUID一样，删除UUID和MAC地址
vi /etc/sysconfig/network-scripts/ifcfg-eth0
删除网卡相关信息的文件
cd /etc/udev/rules.d
rm -f 70-presistent-net.rules
重启系统后，再重启网卡即可
reboot
service network restart