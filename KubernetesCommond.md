# kubernetes集群搭建
192.168.84.34  master01
192.168.84.33  node01
192.168.84.32  node02
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
刷新SWAP
可以执行命令刷新一次SWAP（将SWAP里的数据转储回内存，并清空SWAP里的数据）
swapoff -a && swapon -a
sysctl -p  (执行这个使其生效，不用重启)
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
ssh-copy-id 192.168.84.32
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
