# kubernetes集群搭建
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












