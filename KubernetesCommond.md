# kubernetes集群搭建
192.168.84.32  master01
192.168.84.33  node01
192.168.84.34  node02
https://www.kubernetes.org.cn/4948.html
#设置阿里云 kubernets yum仓库镜像
修改yum安装源
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64/
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF
#kubernetes安装官方文档
https://kubernetes.io/docs/setup/independent/install-kubeadm/#installing-kubeadm-kubelet-and-kubectl
# 关闭selinux防火墙
执行setenforce 0
#selinux配置设置Set SELinux in permissive mode
sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
#centos7设置k8s.conf
cat <<EOF >  /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
sysctl --system
执行命令使修改生效。
modprobe br_netfilter
sysctl -p /etc/sysctl.d/k8s.conf

## Master节点
 Master节点上面主要由四个模块组成，APIServer，schedule,controller-manager,etcd
## Node节点：
每个Node节点主要由三个模板组成：kublet, kube-proxy
## 设置关闭防火墙及SELINUX
systemctl stop firewalld && systemctl disable firewalld
setenforce 0
vi /etc/selinux/config
SELINUX=disabled
## 关闭Swap
swapoff -a && sysctl -w vm.swappiness=0
vi /etc/fstab
#/dev/mapper/centos-swap swap                    swap    defaults        0 0
临时生效：sysctl -w vm.swappiness=0
永久生效：
echo "vm.swappiness = 0">> /etc/sysctl.conf     （尽量不使用交换分区，注意不是禁用）
重启 reboot
刷新SWAP
可以执行命令刷新一次SWAP（将SWAP里的数据转储回内存，并清空SWAP里的数据）
swapoff -a && swapon -a
sysctl -p  (执行这个使其生效，不用重启)
## kube-proxy开启ipvs的前置条件
由于ipvs已经加入到了内核的主干，所以为kube-proxy开启ipvs的前提需要加载以下的内核模块：
ip_vs
ip_vs_rr
ip_vs_wrr
ip_vs_sh
nf_conntrack_ipv4
cat > /etc/sysconfig/modules/ipvs.modules <<EOF
#!/bin/bash
modprobe -- ip_vs
modprobe -- ip_vs_rr
modprobe -- ip_vs_wrr
modprobe -- ip_vs_sh
modprobe -- nf_conntrack_ipv4
EOF
chmod 755 /etc/sysconfig/modules/ipvs.modules && bash /etc/sysconfig/modules/ipvs.modules && lsmod | grep -e ip_vs -e nf_conntrack_ipv4
上面脚本创建了的/etc/sysconfig/modules/ipvs.modules文件，保证在节点重启后能自动加载所需模块。
使用lsmod | grep -e ip_vs -e nf_conntrack_ipv4命令查看是否已经正确加载所需的内核模块。
接下来还需要确保各个节点上已经安装了ipset软件包yum install ipset。 为了便于查看ipvs的代理规则，
最好安装一下管理工具ipvsadm yum install ipvsadm。
如果以上前提条件如果不满足，则即使kube-proxy的配置开启了ipvs模式，也会退回到iptables模式。
## 配置服务器的ntp时间钟（保证服务器之间的时间同步）
yum install ntp ntpdate -y
timedatectl status
timedatectl list-timezones | grep Shanghai
timedatectl set-timezone Asia/Hong_Kong
timedatectl set-ntp yes
date
## 各节点主机名和IP加入/etc/hosts解析

127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4
::1         localhost localhost.localdomain localhost6 localhost6.localdomain6
192.168.84.32    k8s-master01
192.168.84.33    k8s-node01
192.168.84.34    k8s-node02
## master节点到各Node节点SSH免密登录。
ssh-keygen
ssh-copy-id 192.168.84.33
ssh-copy-id 192.168.84.34
# 设置Docker所需参数
cat << EOF | tee /etc/sysctl.d/k8s.conf
net.ipv4.ip_forward = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
sysctl -p /etc/sysctl.d/k8s.conf
##执行上边命令报错解决：
modprobe br_netfilter
ls /proc/sys/net/bridge
# 安装 Docker
yum update
## step 1: 安装必要的一些系统工具
 yum install -y yum-utils device-mapper-persistent-data lvm2
## Step 2: 添加软件源信息
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo 或 yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
yum list docker-ce --showduplicates | sort -r  列出docker版本
yum install docker-ce -y
systemctl start docker && systemctl enable docker
## Step 3: 更新并安装Docker-CE
 yum makecache fast
 yum -y install docker-ce
## Step 4: 开启Docker服务
service docker start
## 配置阿里云加速器我的阿里云加速器
使用vi修改 /etc/docker/daemon.json 文件并添加上”registry-mirrors”: [“https://registry.docker-cn.com“]，如下：
vi /etc/docker/daemon.json
{"registry-mirrors":["https://8h5ave1d.mirror.aliyuncs.com"]}
 重启Docker
  配置完之后执行下面的命令，以使docker的配置文件生效
systemctl daemon-reload
systemctl restart docker
docker镜像仓库地址
https://hub.docker.com/r/library
## 启动
systemctl start docker
配置Docker开机自启动
systemctl enable docker
## 安装 kubeadm，kubelet，kubectl
在各节点安装kubeadm，kubelet，kubectl
安装完后，设置kubelet服务开机自启：必须设置Kubelet开机自启动，才能让k8s集群各组件在系统重启后自动运行。
yum install -y kubelet kubeadm kubectl
systemctl enable kubelet && systemctl start kubelet
## 部署集群
有了上面这些基础设置后，就可以开始用kubeadm init部署k8s集群了。
在master上操作
这一步之前确保swap已关闭。
kubeadm init -h可查看帮助信息：
root@k8s-master:~# kubeadm  init -h
 ##查看init可用的参数，这里使用这两个参数：
   --pod-network-cidr string ： 自定义Pod网络
   --ignore-preflight-errors strings： 忽略一些错误
主节点上执行初始化
kubeadm init \
    --apiserver-advertise-address=192.168.84.32 \
    --image-repository registry.aliyuncs.com/google_containers \
    --kubernetes-version v1.13.1 \
    --pod-network-cidr=10.244.0.0/16

--image-repository  Kubenetes默认Registries地址是k8s.gcr.io，在国内并不能访问gcr.io，在1.13版本中我们可以增加–image-repository参数，默认值是k8s.gcr.io，将其指定为阿里云镜像地址：registry.aliyuncs.com/google_containers。
--apiserver-advertise-address=192.168.84.32 主节点ip  指明用 Master 的哪个 interface 与 Cluster 的其他节点通信。如果 Master 有多个 interface，建议明确指定，如果不指定，kubeadm 会自动选择有默认网关的 interface。
--kubernetes-version v1.13.1   k8s版本关闭版本探测，因为它的默认值是stable-1，会导致从https://dl.k8s.io/release/stable-1.txt下载最新的版本号，我们可以将其指定为固定版本（最新版：v1.13.0）来跳过网络请求。
--pod-network-cidr=10.244.0.0/16  指定 Pod 网络的范围。Kubernetes 支持多种网络方案，而且不同网络方案对 --pod-network-cidr 有自己的要求，
                                    这里设置为 10.244.0.0/16 是因为我们将使用 flannel 网络方案，必须设置成这个 CIDR。
# 在Node上操作node加入集群 （执行主节点初始化最后的命令）
#复制在master节点上记录下的那句话，以加入集群
kubeadm join
## 在主机上执行 kubectl get nodes 查看node信息出错
解决 ： the server doesn't have a resource type "nodes"
        cp /etc/kubernetes/admin.conf ~/.kube/config
需要开启api server 代理端口：
查看端口是否代理：curl localhost:8080/api
开启端口代理：kubectl proxy --port=8080 &
去除master的taint，使用master也能被调度pod
kubectl taint nodes k8s-master node-role.kubernetes.io/master-node/k8s-master untainted

## 各Node节点处于"NotReady" ，需要安装一个CNI网络插件：calico 或flannel
## master上部署flannel
kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

wget https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel-rbac.yml
wget https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
kubectl apply -f kube-fannel-rbac.yml
kubectl apply -f kube-flannel.yml
## master上部署calico
kubectl apply -f calico.yaml
kubectl apply -f rbac.yaml

几分钟后，各Node全部Ready：
#各节点已正常运行
root@k8s-master:~# kubectl get node
至此，所有组件全部运行：
root@k8s-master:~# kubectl get pod -n kube-system
测试集群
配置kubectl的命令补全功能
命令补全功能由安装包"bash-completion"提供，Ubuntu系统中默认已安装。

当前shell生效：
source <(kubectl completion bash)
永久生效：
echo "source <(kubectl completion bash)" >> ~/.bashrc
启动一个pod验证集群是否正常运行。
#run一个deployment
kubectl run -h
Usage:
  kubectl run NAME --image=image [--env="key=value"] [--port=port] [--replicas=replicas] [--dry-run=bool]
[--overrides=inline-json] [--command] -- [COMMAND] [args...] [options]
启动一个nginx

kubectl run nginx --image=nginx:1.10 --port=80
deployment.apps/nginx created
#查看
root@k8s-master:~# kubectl get pod -w -o wide
NAME                     READY   STATUS              RESTARTS   AGE   IP                NODE         NOMINATED   NODE
nginx-787b58fd95-p9jwl   0/1     ContainerCreating      0       59s   <none>            k8s-node02      <none>
nginx-787b58fd95-p9jwl   1/1        Running             0       70s   192.168.58.193    k8s-node02      <none>
测试nginx正常访问
root@k8s-master:~# curl  -I 192.168.58.193
HTTP/1.1 200 OK
Server: nginx/1.10.3
Date: Sat, 29 Sep 2018 02:42:06 GMT
Content-Type: text/html
Content-Length: 612
Last-Modified: Tue, 31 Jan 2017 15:01:11 GMT
Connection: keep-alive
ETag: "5890a6b7-264"
Accept-Ranges: bytes
## 创建一个service
kubectl expose deployment nginx --name=nginx-service --port=80 --target-port=80 --protocol=TCP --type=NodePort
kubectl expose  nginx --port=80 --type=LoadBalancer
kubectl expose -h
Usage:
kubectl expose (-f FILENAME | TYPE NAME) [--port=port] [--protocol=TCP|UDP|SCTP] [--target-port=number-or-name]
[--name=name] [--external-ip=external-ip-of-service] [--type=type] [options]
root@k8s-master:~# kubectl expose deployment nginx --port=801 --target-port=80 --type=NodePort --name nginx-svc
service/nginx-svc exposed
root@k8s-master:~#
查看服务：
root@k8s-master:~# kubectl get svc
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)         AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP         16h
nginx-svc    NodePort    10.100.84.207   <none>        801:30864/TCP   25s
现在可以访问任意Node的30864端口访问到nginx服务：

root@k8s-node01:~# curl 10.3.1.21:30864

<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
    body {
        width: 35em;
        margin: 0 auto;
        font-family: Tahoma, Verdana, Arial, sans-serif;
    }
</style>
</head>
<body>
如果发现哪个某个Node端口无法访问，则设置默认FORWARD规则为ACCEPT
 iptables -P FORWARD ACCEPT

## 安装 kubernetes-dashboard
kubectl apply -f kubernetes-dashboard.yaml 默认的镜像国内网下载不了修改下
## 删除 kubernetes-dashboard
kubectl delete -f kubernetes-dashboard.yaml
## 想要访问dashboard服务，就要有访问权限，这里需要先设置一个dashboard服务的权限和绑定关系，执行以下命令创建对应的资源文件dashboard-svc-account.yaml
执行命令创建ServiceAccount和ClusterRoleBinding：
kubectl create -f ~/dashboard-svc-account.yaml
找出secret，这个secret中有token，该token是登录dashboard时用到的：
kubectl -n kube-system get secret | grep kubernetes-dashboard-admin
执行的结果例：kubernetes-dashboard-admin-token-wc5tf就是dashboard的secret
查看kubernetes-dashboard-admin-token-wc5tf的详情，里面有对应的token信息：
kubectl describe -n kube-system secret/kubernetes-dashboard-admin-token-wc5tf |grep token:
token:右侧的"eyJhbGciOiJSU…"这一长串字符串就是token，这是个永久生效的token，请保存下来：
接下来需要知道dashboard对应的pod是部署在哪个node上的，执行命令：
kubectl get pods -n kube-system | grep kubernetes-dashboard-
在控制台输出如下：
[root@localhost ~]# kubectl get pods -n kube-system > | grep kubernetes-dashboard-
kubernetes-dashboard-77fd78f978-84krd           1/1     Running   0          54m
可见pod的名字是kubernetes-dashboard-77fd78f978-84krd，接下来可以根据名字查看pod的详情；
执行以下命令，用来查看名为"kubernetes-dashboard-77fd78f978-84krd"的pod的详情：
kubectl describe -n kube-system pod/kubernetes-dashboard-77fd78f978-84krd
node1节点的IP是192.168.119.156，再加上dashboard的service映射的端口32073，因此在浏览器上访问的dashboard地址为：https://192.168.84.33:32073/#!/login
## 查看
kubectl get services --all-namespaces

token实例:
eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWRhc2hib2FyZC1hZG1pbi10b2tlbi1zNWRxNiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWRhc2hib2FyZC1hZG1pbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6Ijg4NWI5ZWI1LTA5ODMtMTFlOS05ZWZlLTAwMGMyOTM5ODVmMSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWRhc2hib2FyZC1hZG1pbiJ9.DV7ny0lAGlVlqvOK-OXGZYK-JL4l9rfSCmR9mesNQatdBNSXKQf0rpGXbbNwG5R5D7T6ZLA0Id0CQNrhtxHDQ7smYqzi33wWjXUleiTkPg6ybXSpvcjZuPHAu910CqV5CxoNudKgj7vXwuj8Oy-oO3PIW2pWcn3LeiB3O7qDkNjYZaxHuQtyfQLFgPSGZsQGv73YOxghRZSoFF_cvIY_r5MKNecTSXhc8yfd_M_GMs5ZAdZwGGo7yyUDGSxIpmdTQNNTKMUmhCpSaIgeHFKKiEzmNyvkmzT1TayFRPCUcFE99Fq9ywEqgsfmGSk_QWbZLm0A3QEX5RSRgJePSQV5Hg

# k8s单点安装
1关闭centos自带的防火墙
 systemctl disable firewalld
 systemctl stop firewalld
yum update
#step 1: 安装必要的一些系统工具
 yum install -y yum-utils device-mapper-persistent-data lvm2
#Step 2: 添加软件源信息
 yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
#设置阿里云 kubernets yum仓库镜像
修改yum安装源
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64/
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF
2.安装etcd和kubernetes软件（会自动安装docker）
yum install -y etcd kubernetes
修改配置文件
Docker配置文件/etc/sysconfig/docker
内容改为如下 OPTIONS=’–selinux-enabled=false --insecure-registry gcr.io’
Kubernetes apiservce配置文件/etc/kubernetes/apiserver
去掉–admission-control中的ServiceAccount
缺少rhsm
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm
chmod +x python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm
- 运行
rpm2cpio python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm | cpio -iv --to-stdout /etc/rhsm/ca/redhat-uep.pem | tee /etc/rhsm/ca/redhat-uep.pem
手动下载使用使用docker pull 拉取镜像
==> docker pull registry.access.redhat.com/rhel7/pod-infrastructure:latest
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
kubectl get pods
kubectl describe pod mysql 查看服务状态
删除pod 并重新创建
kubectl delete -f mysql-rc.yaml
kubectl create -f mysql-rc.yaml
docker ps查看运行的容器：
docker ps | grep mysql
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
即可看到运行的进程
docker ps| grep mysql
或者
systemctl status etcd.service

# 二进制文件安装
下载二进制文件安装包
下载地址 https://github.com/kubernetes/kubernetes/blob/master/CHANGELOG-1.13.md#v1131
安装说明 https://www.kubernetes.org.cn/4963.html
 进入k8s github地址 进入release 点击CHANGELOG-*选择下载组件











