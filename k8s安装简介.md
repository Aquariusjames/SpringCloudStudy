Centos7单机版Kubernete安装
2018年08月20日 20:52:17 牧竹子 阅读数：273
单机版K8S
安装
1.关闭centos自带的防火墙
# systemctl disable firewalld
# systemctl stop firewalld

2.安装etcd和kubernetes软件（会自动安装docker）
yum install -y etcd kubernetes
修改配置文件

Docker配置文件/etc/sysconfig/docker
内容改为如下 OPTIONS=’–selinux-enabled=false --insecure-registry gcr.io’

vim /etc/sysconfig/docker
Kubernetes apiservce配置文件/etc/kubernetes/apiserver
去掉–admission-control中的ServiceAccount

vim /etc/kubernetes/apiserver
启动服务

systemctl start etcd.service
systemctl start docker
systemctl start kube-apiserver.service
systemctl start kube-controller-manager.service
systemctl start kube-scheduler.service
systemctl start kubelet.service
systemctl start kube-proxy.service
mkdir /data

创建pod
vim mysql-rc.yaml
1
贴入如下内容

apiVersion: v1
kind: ReplicationController
metadata:
    name: mysql
spec:
    replicas: 1
    selector:
        app: mysql
    template:
        metadata:
            labels:
                app: mysql
        spec:
            containers:
                - name: mysql
                  image: mysql
                  ports:
                      - containerPort: 3306
                  env:
                      - name: MYSQL_ROOT_PASSWORD
                        value: "root"
MYSQL_ROOT_PASSWORD 表示root配置密码是root

创建pod

kubectl create -f mysql-rc.yaml
kubectl get rc
NAME      DESIRED   CURRENT   READY     AGE
mysql     1         1         0         12m



kubectl get pods

NAME          READY     STATUS              RESTARTS   AGE
mysql-zqgck   0/1       ContainerCreating   0          13m
发现status一直处于ContainerCreating状态！！！

kubectl describe pod mysql （该命令用于排查上面状态一直卡在ContainerCreating的情况）

Name:       mysql-zqgck
Namespace:  default
Node:       127.0.0.1/127.0.0.1
Start Time: Tue, 05 Jun 2018 10:25:31 -0400
Labels:     app=mysql
Status:     Pending
IP:
Controllers:    ReplicationController/mysql
Containers:
  mysql:
    Container ID:
    Image:      mysql
    Image ID:
    Port:       3306/TCP
    State:      Waiting
      Reason:       ContainerCreating
    Ready:      False
    Restart Count:  0
    Volume Mounts:  <none>
    Environment Variables:
      MYSQL_ROOT_PASSWORD:  123456
Conditions:
  Type      Status
  Initialized   True
  Ready     False
  PodScheduled  True
No volumes.
QoS Class:  BestEffort
Tolerations:    <none>
Events:
  FirstSeen LastSeen    Count   From            SubObjectPath   Type        Reason      Message
  --------- --------    -----   ----            -------------   --------    ------      -------
  14m       14m     1   {default-scheduler }            Normal      Scheduled   Successfully assigned mysql-zqgck to 127.0.0.1
  14m       3m      7   {kubelet 127.0.0.1}         Warning     FailedSync  Error syncing pod, skipping: failed to "StartContainer" for "POD" with ErrImagePull: "image pull failed for registry.access.redhat.com/rhel7/pod-infrastructure:latest, this may be because there are no credentials on this request.  details: (open /etc/docker/certs.d/registry.access.redhat.com/redhat-ca.crt: no such file or directory)"

  14m   4s  60  {kubelet 127.0.0.1}     Warning FailedSync  Error syncing pod, skipping: failed to "StartContainer" for "POD" with ImagePullBackOff: "Back-off pulling image \"registry.access.redhat.com/rhel7/pod-infrastructure:latest\""

问题处理：手动下载使用使用docker pull 拉取镜像

==> docker pull registry.access.redhat.com/rhel7/pod-infrastructure:latest
1
如果缺少rhsm

wget http://mirror.centos.org/centos/7/os/x86_64/Packages/python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm
chmod +x python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm
- 运行
rpm2cpio python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm | cpio -iv --to-stdout /etc/rhsm/ca/redhat-uep.pem | tee /etc/rhsm/ca/redhat-uep.pem

删除pod 并重新创建

kubectl delete -f mysql-rc.yaml

kubectl create -f mysql-rc.yaml
查看pod状态如下，running说明已经运行起来了

kubectl get rc
NAME      DESIRED   CURRENT   READY     AGE
mysql     1         1         1         1m
- kubectl get pods
NAME          READY     STATUS    RESTARTS   AGE
mysql-gtcj4   1/1       Running   0          12s
docker ps查看运行的容器：

docker ps | grep mysql
1
创建服务
创建service文件

vim mysql-svc.yaml
1
配置信息


apiVersion: v1
kind: Service
metadata:
    name: mysql
spec:
    ports:
        - port: 3306
    selector:
        app: mysql
创建service

kubectl create -f mysql-svc.yaml
kubectl get svc
NAME         CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
kubernetes   10.254.0.1      <none>        443/TCP    1h
mysql        10.254.65.184   <none>        3306/TCP   14s
即可看到运行的进程

docker ps| grep mysql
或者
systemctl status etcd.service
这个还顺利，直接就ok了

集群
准备
所有的操作均需要root权限，请在执行时加sudo或者su到root账号下，在安装的时候请记得把防火墙关闭并设置iptables开放2379端口和8080端口。如果在实验环境下可以直接将iptables也关闭了。

集群环境介绍
至少三台机器

三台主机的职责如下所示：

IP	节点	etcd server	kubernetes server	kubernetes client	docker
192.168.163.148	master	Yes	Yes	Yes	Yes
192.168.163.150	node	-	-	Yes	Yes
192.168.163.138	node	-	-	Yes	Yes
单机环境

IP	节点	etcd server	kubernetes server	kubernetes client	docker
192.168.163.148	master	Yes	Yes	Yes	Yes
这里我们只安装单机环境，基本流程都包含在这里了。

安装步骤
1）关闭CentOS自带的防火墙服务：

systemctl disable firewalld
systemctl stop firewalld

如果你之前安装过docker，请先卸载，重复安装会报错，如下：
docker-ce conflicts with 2:docker-1.13.1-68
错误原因：（https://stackoverflow.com/questions/44891775/kubernetes-installation-on-centos7）

According to the documentation, Kubernetes is not yet compatible with docker-ce (docker >=17.x)

On each of your machines, install Docker. Version 1.12 is recommended, but v1.10 and v1.11 are known to work as well. Versions 1.13 and 17.03+ have not yet been tested and verified by the Kubernetes node team.

解决方法：
卸载docker-ce步骤如下

1、查看安装过的docker：yum list installed | grep docker

2、卸载docker：yum remove -y docker-ce.x86_64 0:18.03.0.ce-1.el7.centos

3、删除容器镜像：rm -rf /var/lib/docker

##安装etcd和Kubernetes软件

全安装命令（单机版，会自动依赖安装Docker软件）
yum install -y etcd kubernetes

master只装主节点的方式如下：
yum install etcd kubernetes-master docker-y

node节点（不需要etc和kub-master）
yum install kubernetes-node flannel docker -y

安装完成结果如下

Installed:
etcd.x86_64 0:3.2.22-1.el7 kubernetes.x86_64 0:1.5.2-0.7.git269f928.el7

Dependency Installed:
conntrack-tools.x86_64 0:1.4.4-3.el7_3 container-storage-setup.noarch 0:0.10.0-1.gitdf0dcd5.el7 device-mapper-event.x86_64 7:1.02.146-4.el7
device-mapper-event-libs.x86_64 7:1.02.146-4.el7 device-mapper-persistent-data.x86_64 0:0.7.3-3.el7 docker.x86_64 2:1.13.1-68.gitdded712.el7.centos
docker-client.x86_64 2:1.13.1-68.gitdded712.el7.centos docker-common.x86_64 2:1.13.1-68.gitdded712.el7.centos kubernetes-client.x86_64 0:1.5.2-0.7.git269f928.el7
kubernetes-master.x86_64 0:1.5.2-0.7.git269f928.el7 kubernetes-node.x86_64 0:1.5.2-0.7.git269f928.el7 libaio.x86_64 0:0.3.109-13.el7
libnetfilter_cthelper.x86_64 0:1.0.0-9.el7 libnetfilter_cttimeout.x86_64 0:1.0.0-6.el7 libnetfilter_queue.x86_64 0:1.0.2-2.el7_2
lvm2.x86_64 7:2.02.177-4.el7 lvm2-libs.x86_64 7:2.02.177-4.el7 oci-register-machine.x86_64 1:0-6.git2b44233.el7
oci-systemd-hook.x86_64 1:0.1.16-1.git05bd9a0.el7 oci-umount.x86_64 2:2.3.3-3.gite3c9055.el7 skopeo-containers.x86_64 1:0.1.31-1.dev.gitae64ff7.el7.centos
socat.x86_64 0:1.7.3.2-2.el7 yajl.x86_64 0:2.0.4-4.el7

Dependency Updated:
container-selinux.noarch 2:2.66-1.el7 device-mapper.x86_64 7:1.02.146-4.el7 device-mapper-libs.x86_64 7:1.02.146-4.el7 libselinux.x86_64 0:2.5-12.el7
libselinux-python.x86_64 0:2.5-12.el7 libselinux-utils.x86_64 0:2.5-12.el7 libsemanage.x86_64 0:2.5-11.el7 libsemanage-python.x86_64 0:2.5-11.el7
libsepol.x86_64 0:2.5-8.1.el7 policycoreutils.x86_64 0:2.5-22.el7 policycoreutils-python.x86_64 0:2.5-22.el7 selinux-policy.noarch 0:3.13.1-192.el7_5.4
selinux-policy-targeted.noarch 0:3.13.1-192.el7_5.4 setools-libs.x86_64 0:3.3.8-2.el7

Complete!

配置文件修改
安装好软件后，修改2个配置文件（其他配置文件使用系统默认的参数即可）

1)docker配置文件为 /etc/sysconfig/docker，其中OPTIONS的内容设置为

OPTION=’–selinux-enable=false --insecure-registry gcr.io’

关闭selinux，安全增强型 Linux（Security-Enhanced Linux）简称 SELinux，它是一个 Linux 内核模块，也是 Linux 的一个安全子系统。

如果服务器可以访问外网，则可在docker daemon的启动参数中加上–insecure-registry gcr.io

2)Kubernetes apiserver 配置文件为/etc/kubernetes/apiserver，把KUBE_ADMISSION_CONTROL参数中的ServiceAccount删除。

# default admission control policies
KUBE_ADMISSION_CONTROL="--admission-control=NamespaceLifecycle,NamespaceExists,LimitRanger,SecurityContextDeny,ServiceAccount,ResourceQuota"
1
2
kubernetes安全机制–Admission Control 准入控制

在kubernetes中，一些高级特性正常运行的前提条件为，将一些准入模块处于enable状态。总结下，对于kubernetes apiserver，如果不适当的配置准入控制模块，他就不能称作是一个完整的server，某些功能也不会正常的生效。

ServiceAccount

一个serviceAccount为运行在pod内的进程添加了相应的认证信息。

当准入模块中开启了此插件（默认开启），如果pod没有serviceAccount属性，将这个pod的serviceAccount属性设为“default”；确保pod使用的serviceAccount始终存在；

如果LimitSecretReferences 设置为true，当这个pod引用了Secret对象却没引用ServiceAccount对象，弃置这个pod；

如果这个pod没有包含任何ImagePullSecrets，则serviceAccount的ImagePullSecrets被添加给这个pod；
如果MountServiceAccountToken为true，则将pod中的container添加一个VolumeMount 。

4）按顺序启动所有的服务：

systemctl start etcd

systemctl start docker

systemctl start kube-apiserver

systemctl start kube-controller-manager

systemctl start kube-scheduler

systemctl start kubelet

systemctl start kube-proxy

至此，一个单机版的Kubernetes集群环境就安装启动完成了。

查看节点数量

#kubectl get nodes
NAME        STATUS    AGE
127.0.0.1   Ready     1m
可以看到只有一个节点

参考资料
单机版参考
https://blog.csdn.net/yjf2013/article/details/80609468
集群搭建参考
https://blog.csdn.net/yjf2013/article/details/80636475
https://blog.csdn.net/yang7551735/article/details/51172179