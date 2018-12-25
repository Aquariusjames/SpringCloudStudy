# kubernetes集群搭建
192.168.84.32  master01
192.168.84.33  node01
192.168.84.34  node02
https://www.kubernetes.org.cn/4948.html
#设置阿里云 kubernets yum仓库镜像
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernets]
name=kubernetes
baseusrl=http://mirrors.aliyun.com/kubernets/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg
       http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF
#kubernetes安装官方文档
https://kubernetes.io/docs/setup/independent/install-kubeadm/#installing-kubeadm-kubelet-and-kubectl
#关闭selinux防火墙
执行setenforce 0
#selinux配置设置Set SELinux in permissive mode
sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
#centos7设置k8s.conf
cat <<EOF >  /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
sysctl --system
#yum安装kubelet kubeadm kubectl
yum install -y kubelet kubeadm kubectl --disableexcludes=kubernetes
#设置开机自启
systemctl enable kubelet && systemctl start kubelet

systemctl daemon-reload
systemctl restart kubelet
#初始化
kubeadm init --pod-network-cidr=10.244.0.0/16 --service-cidr=10.96.0.0/12 --ignore-preflight-errors=Swap

# 二进制文件安装
下载二进制文件安装包
下载地址 https://github.com/kubernetes/kubernetes/blob/master/CHANGELOG-1.13.md#v1131
安装说明 https://www.kubernetes.org.cn/4963.html
 进入k8s github地址 进入release 点击CHANGELOG-*选择下载组件
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
#UUID=7bff6243-324c-4587-b550-55dc34018ebf swap                    swap    defaults        0 0
临时生效：sysctl -w vm.swappiness=0
永久生效：
echo "vm.swappiness = 0">> /etc/sysctl.conf     （尽量不使用交换分区，注意不是禁用）
重启 reboot
刷新SWAP
可以执行命令刷新一次SWAP（将SWAP里的数据转储回内存，并清空SWAP里的数据）
swapoff -a && swapon -a
sysctl -p  (执行这个使其生效，不用重启)
## 配置服务器的ntp时间钟（保证服务器之间的时间同步）
yum install ntp ntpdate -y
timedatectl status
timedatectl list-timezones | grep Shanghai
timedatectl set-timezone Asia/Hong_Kong
timedatectl set-ntp yes
date
## 各节点主机名和IP加入/etc/hosts解析

#127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4
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
## 安装kubeadm工具
yum  install -y  kubeadm
## 安装完后，设置kubelet服务开机自启：必须设置Kubelet开机自启动，才能让k8s集群各组件在系统重启后自动运行。
systemctl enable kubelet
## 部署集群
有了上面这些基础设置后，就可以开始用kubeadm init部署k8s集群了。
在master上操作
这一步之前确保swap已关闭。
kubeadm init -h可查看帮助信息：
root@k8s-master:~# kubeadm  init -h
 ##查看init可用的参数，这里使用这两个参数：
   --pod-network-cidr string ： 自定义Pod网络
   --ignore-preflight-errors strings： 忽略一些错误
开始初始化集群
root@k8s-master:~# kubeadm init  --pod-network-cidr 192.168.0.0/16 --ignore-preflight-errors=all
## 初始化完成，一台Master节点就部署好了，初始化过程中需要一定时间来pull镜像，也可以使用下面的命令提前下载好镜像：
 kubeadm  config images pull
## 根据提示执行：
mkdir -p $HOME/.kube
cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
chown $(id -u):$(id -g) $HOME/.kube/config
# 在Node上操作
## 在所有Node上使用kubeadm join加入集群：
#确保swap已关闭
#复制在master节点上记录下的那句话，以加入集群
kubeadm join 192.168.84.33:6443 --token mwfr7m.57rmd56ghjyu0716 --discovery-token-ca-cert-hash sha256:8fbd33519b0203e9aa03cc882cb5489b5e6ad455f97581b1abf8ceb1dca8f622
nit完后，节点已加入群集。
最后，在master节点查看：
kubectl get node
去除master的taint，使用master也能被调度pod
kubectl taint nodes k8s-master node-role.kubernetes.io/master-node/k8s-master untainted
## 各Node节点处于"NotReady" ，需要安装一个CNI网络插件：
kubectl apply -f https://docs.projectcalico.org/v3.1/getting-started/kubernetes/installation/hosted/kubeadm/1.7/calico.yaml
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
NAME                     READY   STATUS              RESTARTS   AGE   IP       NODE         NOMINATED NODE
nginx-787b58fd95-p9jwl   0/1     ContainerCreating   0          59s   <none>   k8s-node02   <none>
nginx-787b58fd95-p9jwl   1/1   Running   0     70s   192.168.58.193   k8s-node02   <none>
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
把nginx暴露一个端口出来，以使集群之外能访问
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




## 创建安装目录
mkdir /k8s/etcd/{bin,cfg,ssl} -p
mkdir /k8s/kubernetes/{bin,cfg,ssl} -p

## 安装及配置CFSSL
yum install wget
wget https://pkg.cfssl.org/R1.2/cfssl_linux-amd64
wget https://pkg.cfssl.org/R1.2/cfssljson_linux-amd64
wget https://pkg.cfssl.org/R1.2/cfssl-certinfo_linux-amd64
chmod +x cfssl_linux-amd64 cfssljson_linux-amd64 cfssl-certinfo_linux-amd64
mv cfssl_linux-amd64 /usr/local/bin/cfssl
mv cfssljson_linux-amd64 /usr/local/bin/cfssljson
mv cfssl-certinfo_linux-amd64 /usr/bin/cfssl-certinfo
## 创建认证证书
创建 ETCD 证书
cat << EOF | tee ca-config.json
{
  "signing": {
    "default": {
      "expiry": "87600h"
    },
    "profiles": {
      "www": {
         "expiry": "87600h",
         "usages": [
            "signing",
            "key encipherment",
            "server auth",
            "client auth"
        ]
      }
    }
  }
}
EOF

## 创建 ETCD CA 配置文件
cat << EOF | tee ca-csr.json
{
    "CN": "etcd CA",
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C": "CN",
            "L": "Shenzhen",
            "ST": "Shenzhen"
        }
    ]
}
EOF

## 创建 ETCD Server 证书
cat << EOF | tee server-csr.json
   {
       "CN": "etcd",
       "hosts": [
       "192.168.84.34",
       "192.168.84.33",
       "192.168.84.32"
       ],
       "key": {
           "algo": "rsa",
           "size": 2048
       },
       "names": [
           {
               "C": "CN",
               "L": "Shenzhen",
               "ST": "Shenzhen"
           }
       ]
   }
   EOF

## 生成 ETCD CA 证书和私钥
cfssl gencert -initca ca-csr.json | cfssljson -bare ca -
cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=www server-csr.json | cfssljson -bare server
创建 Kubernetes CA 证书

## 创建 Kubernetes CA 证书
cat << EOF | tee ca-config.json
{
  "signing": {
    "default": {
      "expiry": "87600h"
    },
    "profiles": {
      "kubernetes": {
         "expiry": "87600h",
         "usages": [
            "signing",
            "key encipherment",
            "server auth",
            "client auth"
        ]
      }
    }
  }
}
EOF
cat << EOF | tee ca-csr.json
{
    "CN": "kubernetes",
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C": "CN",
            "L": "Shenzhen",
            "ST": "Shenzhen",
            "O": "k8s",
            "OU": "System"
        }
    ]
}
EOF
cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes server-csr.json | cfssljson -bare server

## 创建 Kubernetes Proxy 证书
cat << EOF | tee kube-proxy-csr.json
{
  "CN": "system:kube-proxy",
  "hosts": [],
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "L": "Shenzhen",
      "ST": "Shenzhen",
      "O": "k8s",
      "OU": "System"
    }
  ]
}
EOF
cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes kube-proxy-csr.json | cfssljson -bare kube-proxy

## ssh-key认证
ssh-keygen
ssh-copy-id 192.168.84.33
ssh-copy-id 192.168.84.34
# 部署ETCD
## 解压安装文件
tar -xvf etcd-v3.3.10-linux-amd64.tar.gz
cd etcd-v3.3.10-linux-amd64/
cp etcd etcdctl /k8s/etcd/bin/

yum install -y vim
vim /k8s/etcd/cfg/etcd

#[Member]
ETCD_NAME="etcd01"
ETCD_DATA_DIR="/var/lib/etcd/default.etcd"
ETCD_LISTEN_PEER_URLS="https://192.168.84.34:2380"
ETCD_LISTEN_CLIENT_URLS="https://192.168.84.34:2379"

#[Clustering]
ETCD_INITIAL_ADVERTISE_PEER_URLS="https://192.168.84.34:2380"
ETCD_ADVERTISE_CLIENT_URLS="https://192.168.84.34:2379"
ETCD_INITIAL_CLUSTER="etcd01=https://192.168.84.34:2380,etcd02=https://192.168.84.33:2380,etcd03=https://192.168.84.32:2380"
ETCD_INITIAL_CLUSTER_TOKEN="etcd-cluster"
ETCD_INITIAL_CLUSTER_STATE="new"

## 创建 etcd的 systemd unit 文件
vim /usr/lib/systemd/system/etcd.service
[Unit]
Description=Etcd Server
After=network.target
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
EnvironmentFile=/k8s/etcd/cfg/etcd
ExecStart=/k8s/etcd/bin/etcd \
--name=${ETCD_NAME} \
--data-dir=${ETCD_DATA_DIR} \
--listen-peer-urls=${ETCD_LISTEN_PEER_URLS} \
--listen-client-urls=${ETCD_LISTEN_CLIENT_URLS},http://127.0.0.1:2379 \
--advertise-client-urls=${ETCD_ADVERTISE_CLIENT_URLS} \
--initial-advertise-peer-urls=${ETCD_INITIAL_ADVERTISE_PEER_URLS} \
--initial-cluster=${ETCD_INITIAL_CLUSTER} \
--initial-cluster-token=${ETCD_INITIAL_CLUSTER_TOKEN} \
--initial-cluster-state=new \
--cert-file=/k8s/etcd/ssl/server.pem \
--key-file=/k8s/etcd/ssl/server-key.pem \
--peer-cert-file=/k8s/etcd/ssl/server.pem \
--peer-key-file=/k8s/etcd/ssl/server-key.pem \
--trusted-ca-file=/k8s/etcd/ssl/ca.pem \
--peer-trusted-ca-file=/k8s/etcd/ssl/ca.pem
Restart=on-failure
LimitNOFILE=65536
[Install]
WantedBy=multi-user.target

## 拷贝证书文件
cp ca*pem server*pem /k8s/etcd/ssl
## 启动ETCD服务
systemctl daemon-reload
systemctl enable etcd
systemctl start etcd
## 将启动文件、配置文件拷贝到 节点1、节点2
cd /k8s/
scp -r etcd 192.168.84.32:/k8s/
scp -r etcd 192.168.84.33:/k8s/
scp /usr/lib/systemd/system/etcd.service  192.168.84.32:/usr/lib/systemd/system/etcd.service
scp /usr/lib/systemd/system/etcd.service  192.168.84.33:/usr/lib/systemd/system/etcd.service

vim /k8s/etcd/cfg/etcd
#[Member]
ETCD_NAME="etcd02"
ETCD_DATA_DIR="/var/lib/etcd/default.etcd"
ETCD_LISTEN_PEER_URLS="https://192.168.84.32:2380"
ETCD_LISTEN_CLIENT_URLS="https://192.168.84.32:2379"

#[Clustering]
ETCD_INITIAL_ADVERTISE_PEER_URLS="https://192.168.84.32:2380"
ETCD_ADVERTISE_CLIENT_URLS="https://192.168.84.32:2379"
ETCD_INITIAL_CLUSTER="etcd01=https://192.168.84.34:2380,etcd02=https://192.168.84.32:2380,etcd03=https://192.168.84.33:2380"
ETCD_INITIAL_CLUSTER_TOKEN="etcd-cluster"
ETCD_INITIAL_CLUSTER_STATE="new"

vim /k8s/etcd/cfg/etcd

#[Member]
ETCD_NAME="etcd03"
ETCD_DATA_DIR="/var/lib/etcd/default.etcd"
ETCD_LISTEN_PEER_URLS="https://192.168.84.33:2380"
ETCD_LISTEN_CLIENT_URLS="https://192.168.84.33:2379"

#[Clustering]
ETCD_INITIAL_ADVERTISE_PEER_URLS="https://192.168.84.33:2380"
ETCD_ADVERTISE_CLIENT_URLS="https://192.168.84.33:2379"
ETCD_INITIAL_CLUSTER="etcd01=https://192.168.84.34:2380,etcd02=https://192.168.84.32:2380,etcd03=https://192.168.84.33:2380"
ETCD_INITIAL_CLUSTER_TOKEN="etcd-cluster"
ETCD_INITIAL_CLUSTER_STATE="new"

## 验证集群是否正常运行
./etcdctl \
--ca-file=/k8s/etcd/ssl/ca.pem \
--cert-file=/k8s/etcd/ssl/server.pem \
--key-file=/k8s/etcd/ssl/server-key.pem \
--endpoints="https://192.168.84.34:2379,\
https://192.168.84.32:2379,\
https://192.168.84.33:2379" cluster-health

注意：
启动ETCD集群同时启动二个节点，启动一个节点集群是无法正常启动的；
## 部署Flannel网络
向 etcd 写入集群 Pod 网段信息
cd /k8s/etcd/ssl/
/k8s/etcd/bin/etcdctl \
--ca-file=ca.pem --cert-file=server.pem \
--key-file=server-key.pem \
--endpoints="https://192.168.84.32:2379,\
https://192.168.84.33:2379,https://192.168.84.34:2379" \
set /coreos.com/network/config  '{ "Network": "192.18.0.0/16", "Backend": {"Type": "vxlan"}}'
flanneld 当前版本 (v0.10.0) 不支持 etcd v3，故使用 etcd v2 API 写入配置 key 和网段数据；
写入的 Pod 网段 ${CLUSTER_CIDR} 必须是 /16 段地址，必须与 kube-controller-manager 的 --cluster-cidr 参数值一致；

tar -xvf flannel-v0.10.0-linux-amd64.tar.gz
mv flanneld mk-docker-opts.sh /k8s/kubernetes/bin/
## 配置Flannel
vim /k8s/kubernetes/cfg/flanneld
FLANNEL_OPTIONS="--etcd-endpoints=https://192.168.84.34:2379,https://192.168.84.33:2379,https://192.168.84.32:2379 -etcd-cafile=/k8s/etcd/ssl/ca.pem -etcd-certfile=/k8s/etcd/ssl/server.pem -etcd-keyfile=/k8s/etcd/ssl/server-key.pem"
## 创建 flanneld 的 systemd unit 文件
vim /usr/lib/systemd/system/flanneld.service
[Unit]
Description=Flanneld overlay address etcd agent
After=network-online.target network.target
Before=docker.service

[Service]
Type=notify
EnvironmentFile=/k8s/kubernetes/cfg/flanneld
ExecStart=/k8s/kubernetes/bin/flanneld --ip-masq $FLANNEL_OPTIONS
ExecStartPost=/k8s/kubernetes/bin/mk-docker-opts.sh -k DOCKER_NETWORK_OPTIONS -d /run/flannel/subnet.env
Restart=on-failure

[Install]
WantedBy=multi-user.target

mk-docker-opts.sh 脚本将分配给 flanneld 的 Pod 子网网段信息写入 /run/flannel/docker 文件，后续 docker 启动时 使用这个文件中的环境变量配置 docker0 网桥；
flanneld 使用系统缺省路由所在的接口与其它节点通信，对于有多个网络接口（如内网和公网）的节点，可以用 -iface 参数指定通信接口，如上面的 eth0 接口;
flanneld 运行时需要 root 权限；

## 配置Docker启动指定子网段
vim /usr/lib/systemd/system/docker.service
[Unit]
Description=Docker Application Container Engine
Documentation=https://docs.docker.com
After=network-online.target firewalld.service
Wants=network-online.target

[Service]
Type=notify
EnvironmentFile=/run/flannel/subnet.env
ExecStart=/usr/bin/dockerd $DOCKER_NETWORK_OPTIONS
ExecReload=/bin/kill -s HUP $MAINPID
LimitNOFILE=infinity
LimitNPROC=infinity
LimitCORE=infinity
TimeoutStartSec=0
Delegate=yes
KillMode=process
Restart=on-failure
StartLimitBurst=3
StartLimitInterval=60s

[Install]
WantedBy=multi-user.target
## 将flanneld systemd unit 文件到所有节点
cd /k8s/
scp -r kubernetes 192.168.84.33:/k8s/
scp -r kubernetes 192.168.84.32:/k8s/
scp /k8s/kubernetes/cfg/flanneld 192.168.84.33:/k8s/kubernetes/cfg/flanneld
scp /k8s/kubernetes/cfg/flanneld 192.168.84.32:/k8s/kubernetes/cfg/flanneld
scp /usr/lib/systemd/system/docker.service  192.168.84.33:/usr/lib/systemd/system/docker.service
scp /usr/lib/systemd/system/docker.service  192.168.84.32:/usr/lib/systemd/system/docker.service
scp /usr/lib/systemd/system/flanneld.service  192.168.84.33:/usr/lib/systemd/system/flanneld.service
scp /usr/lib/systemd/system/flanneld.service  192.168.84.32:/usr/lib/systemd/system/flanneld.service

启动服务
systemctl daemon-reload
systemctl start flanneld
systemctl enable flanneld
systemctl restart docker
