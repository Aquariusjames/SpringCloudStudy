># docker安装
使用rpm包安装docker
1）稳定版下载地址：
https://download.docker.com/linux/centos/7/x86_64/stable/Packages/

CentOS 7 (使用yum进行安装)
yum update
#step 1: 安装必要的一些系统工具
 yum install -y yum-utils device-mapper-persistent-data lvm2
#Step 2: 添加软件源信息
 yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
#Step 3: 更新并安装Docker-CE
 yum makecache fast
 yum -y install docker-ce
#Step 4: 开启Docker服务
service docker start
 注意：
 官方软件源默认启用了最新的软件，您可以通过编辑软件源的方式获取各个版本的软件包。例如官方并没有将测试版本的软件源置为可用，您可以通过以下方式开启。同理可以开启各种测试版本等。
 vim /etc/yum.repos.d/docker-ee.repo
   将[docker-ce-test]下方的enabled=0修改为enabled=1
# 安装指定版本的Docker-CE:
 Step 1: 查找Docker-CE的版本:
 yum list docker-ce.x86_64 --showduplicates | sort -r
   Loading mirror speeds from cached hostfile
   Loaded plugins: branch, fastestmirror, langpacks
   docker-ce.x86_64            17.03.1.ce-1.el7.centos            docker-ce-stable
  docker-ce.x86_64            17.03.1.ce-1.el7.centos            @docker-ce-stable
   docker-ce.x86_64            17.03.0.ce-1.el7.centos            docker-ce-stable
   Available Packages
 Step2: 安装指定版本的Docker-CE: (VERSION例如上面的17.03.0.ce.1-1.el7.centos)
 yum -y install docker-ce-[VERSION]
安装校验
root@iZbp12adskpuoxodbkqzjfZ:$ docker version
我的第一个docker仓库地址 https://hub.docker.com/r/wozhuchenfu/mydockerrepositoryfirst/
用户名   wozhuchenfu
密码     qijingyu12345678
docker官方镜像仓库 https://hub.docker.com

配置Docker中国区官方镜像
  使用vi修改 /etc/docker/daemon.json 文件并添加上”registry-mirrors”: [“https://registry.docker-cn.com“]，如下：

vi /etc/docker/daemon.json
{
"registry-mirrors": ["https://registry.docker-cn.com"]
}
#配置阿里云加速器我的阿里云加速器
{"registry-mirrors":["https://8h5ave1d.mirror.aliyuncs.com"]}
 重启Docker
  配置完之后执行下面的命令，以使docker的配置文件生效
systemctl daemon-reload
systemctl restart docker
docker镜像仓库地址
https://hub.docker.com/r/library

启动
systemctl start docker
配置Docker开机自启动
systemctl enable docker

># 拉取mysql镜像
##docker pull mysql:tag
 #设置主机网络 不设置有可能连接不到服务
 [创建容器的时候报错WARNING: IPv4 forwarding is disabled. Networking will not work.]
解决办法：vim  /usr/lib/sysctl.d/00-system.conf 添加如下代码：net.ipv4.ip_forward=1 重启network服务 systemctl restart network

##启动 docker run -it --name mysql -e MYSQL_ROOT_PASSWORD=password -d mysql:tag
设置数据库参数
docker run --name mysqlwp -e MYSQL_ROOT_PASSWORD=wordpressdocker \
-e MYSQL_DATABASE=wordpress \
-e MYSQL_USER=wordpress \
-e MYSQL_PASSWORD=wordpresspwd \
-d mysql

##通过 MYSQL_ROOT_PASSWORD 环境变量来设置 MySQL 的密码

>## 启动一个镜像连接mysql
##docker run -it --name imagename --link mysql:mysql -p 80:80 -d imagename:tag
 启动容器并指定设置好的数据库表
docker run --name imagename --link mysqlwp:mysql -p 80:80 \
-e WORDPRESS_DB_NAME=wordpress \
-e WORDPRESS_DB_USER=wordpress \
-e WORDPRESS_DB_PASSWORD=wordpresspwd \
-d imagname:tag

 docker inspect [CONTAINER ID] 查看启动的容器的详细信息
# 删除容器
 docker stop containername:tag
 docker rm containername:tag
 删除所有容器
 docker stop $(docker ps -a)
 docker rm -v $(docker ps -aq) docker rm 命令的 -v 选项用来删除 MySQL 镜像中定义的数据卷
# 备份在容器中运行的数据库
解决方案：一是可以在一个以后台方式运行的容器中执行一条命令，二是挂载一个宿主机卷（即一个在宿主机上能访问的存储区域）到容器中
将 Docker 主机上的卷挂载到 MySQL 容器中
使用 docker exec 命令执行 mysqldump
容器停止数据不会丢失当容器被删除那么容器中的数据就会丢失
有一种方式可以在容器被 docker rm -v 命令删除之后也能保留数据，就是将宿主机的卷
挂载到容器中。如果你只是使用 docker rm 命令来删除容器，那么这个容器的镜像所定义
的卷会在磁盘上保留，尽管这时容器已经不存在了。如果看一下构建这个 MySQL 镜像的
Dockerfile 文 件（https://github.com/docker-library/mysql/blob/d6268ace61047c74468d7c59b4d8da6be5dec16a/5.6/Dockerfile），将会看到 VOLUME /var/lib/mysql 这一行。这一行的意思是，
当你基于该镜像启动一个容器时，可以将宿主机的文件夹绑定到容器中的这个挂载点上，
比如下面这样。
$ docker run --name mysqlwp -e MYSQL_ROOT_PASSWORD=wordpressdocker \
-e MYSQL_DATABASE=wordpress \
-e MYSQL_USER=wordpress \
-e MYSQL_PASSWORD=wordpresspwd \
-v /home/docker/mysql:/var/lib/mysql \
-d mysql
上面命令中 -v /home/docker/mysql:/var/lib/mysql 这一行进行了宿主机和容器中卷的绑
定。当完成 WordPress 的设置之后，在宿主机 /home/docker/mysql 文件夹下就能看到这些
文件变动。
$ ls mysql/
auto.cnf ibdata1 ib_logfile0 ib_logfile1 mysql performance_schema wordpress
为 了 对 整 个 MySQL 数 据 库 进 行 备 份， 可 以 使 用 docker exec 命 令 在 容 器 内 执 行
mysqldump ，如下所示。
$ docker exec mysqlwp mysqldump --all-databases --password=wordpressdocker > wordpress.backup
现在你可以使用传统方式来进行数据库的备份和恢复了。比如，在云环境中，你可能会将
Elastic Block Store（例如 AWS EBS）挂载到一个主机实例，再挂载到容器中。你也可以将
你的 MySQL 备份保存到 Elastic Storage（比如 AWS S3）中。
# 在宿主机和容器之间共享数据
在运行 docker run 命令时，通过设置 -v 选项将宿主机的卷挂载到容器中。
比如，你想将宿主机 /cookbook 目录下的工作目录与容器共享，可以执行以下指令。
$ ls
data
$ docker run -ti -v "$PWD":/cookbook ubuntu:14.04 /bin/bash
root@11769701f6f7:/# ls /cookbook
data
在这个例子中，宿主机上的当前工作目录会挂载到容器中的 /cookbook 目录上。如果你在
容器内创建了文件或者文件夹，那么这些修改会直接反映到宿主机上，如下所示。
$ docker run -ti -v "$PWD":/cookbook ubuntu:14.04 /bin/bash
root@44d71a605b5b:/# touch /cookbook/foobar
root@44d71a605b5b:/# exit
exit
$ ls -l foobar
-rw-r--r-- 1 root root 0 Mar 11 11:42 foobar
默认情况下，Docker 会以读写模式挂载数据卷。如果想以只读方式挂载数据卷，可以在卷
名称后通过冒号设置相应的权限。比如在前面的例子中，如果想以只读方式将工作目录挂
载到 /cookbook，可以使用 -v "$PWD":/cookbook:ro 。
#通过 docker inspect 命令来查看数据卷的挂载映射情况。
docker inspect -f {{.Mounts}} 44d71a605b5b
#删除容器及它的卷
docker rm -v [continaerID]|[containerName]
#获取容器的ip
docker inspect --format '{{ .NetworkSettings.IPAddress }}' 【containerID】
>#安装lsof查看网络 yum install -y lsof
#查看端口占用 lsof -i:端口号
  查看进程 ps -aux|grep tomcat
#在容器之间共享数据（数据卷）
将容器中的卷共享给其他容器，可以使用 --volumes-from 选项
  例：docker run -v /data --name data ubuntu:14.04
      查看数据卷挂载情况 docker inspect -f {{.Mounts}} [containerID]
  这个容器并没有处于运行状态。但是它的卷映射关系已经存在，并且卷被持
  久化到了 /var/lib/docker/vfs/dir 下面。你可以通过 docker rm -v data 命令来
  删除容器和它的卷。如果你没有使用 rm -v 选项来删除容器和它的卷，那么
  系统中将会遗留很多没有被使用的卷。
#即使这个数据容器没有运行，你也可以通过 --volumes-from 来挂载其中的卷
 例：docker run -it --volumes-from data ubuntu:14.04 /bin/bash
     touch /data/foobar
     exit
     ls /var/lib/docker/volumes/...
# 对容器进行数据复制
你有一个运行中的容器，没有设置任何卷映射信息，但是你想从容器内复制数据出来或者
将数据复制到容器里。
 使用 docker cp 命令将文件从正在运行的容器复制到Docker主机。 docker cp 命令支持在 Docker主机与容器之间进行文件复制。
 例：首先启动一个容器（或进入启动中的容器） docker run -d  --name testcopy ubuntu:14.04 sleep 360 启动一个容器并执行睡眠操作
     进入容器 docker exec -it testcopy bash
        cd /root
        echo 'I am in the container' > file.txt
        exit
     将在容器中创建的这个文件复制到宿主机上，使用 docker cp命令
        docker cp testcopy:/root/file.txt .  复制到宿主机当前目录
     查看复制文件 cat file.txt
  将文件从宿主机复制到容器，仍然可以用docker cp命令只不过源和目的文件换一下位置
    echo 'I am in the host' > host.txt
    cat host.txt
    docker cp host.txt testcopy:/root/host.txt
  两个容器间文件的复制：可以利用宿主机作为中转站，执行两次docker cp命令实现
    docker cp c1:/root/file.txt .
    docker file.txt c2:/root/file.txt
# docker java容器运行springBoot.jar应用
    例：docker run -it --name java -p 8080:8080(jar包中应用用的端口) -d java:8u111
    拷贝jar包到容器 docker cp /**/springboot.jar java:/root/application.jar
    进入容器 docker exec -it java bash
    启动jar  java -jar /root/application.jar -&
# 导出/入镜像
    导出:
        docker save image.tar [containerIDs.../imageNames...]
        docker save如果指定的是container，docker save将保存的是容器背后的image  docker save可以将多个镜像打包到一个tar包
        docker export [containerID] |[gzip](gzip压缩) > containername.tar
        docker export是用来将container的文件系统进行打包的 docker export需要指定container，不能像docker save那样指定image或container都可以
    导入:
        docker load -i iamge.tar   docker load不能载入容器包
        如果本地镜像库已经存在这个镜像，将会被覆盖
        docker save的应用场景是，如果你的应用是使用docker-compose.yml编排的多个镜像组合，但你要部署的客户服务器并不能连外网。这时，你可以使用docker save将用到的镜像打个包，然后拷贝到客户服务器上使用docker load载入。
        docker import container.tar importcontainername:tag 将打包的container载入进来使用docker import
        docker import --help
        docker import将container导入后会成为一个image，而不是恢复为一个container
# 创建和共享镜像














