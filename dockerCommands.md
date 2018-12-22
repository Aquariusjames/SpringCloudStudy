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

# 设置主机网络 不设置有可能连接不到服务
 [创建容器的时候报错WARNING: IPv4 forwarding is disabled. Networking will not work.]
解决办法：vim  /usr/lib/sysctl.d/00-system.conf 添加如下代码：net.ipv4.ip_forward=1 重启network服务 systemctl restart network

># 拉取mysql镜像
##docker pull mysql:tag

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
#改动容器文件并查看改动
1 进入容器进行修改
2 退出容器利用改动的容器新建镜像 docker commit -a "qi" -m "createmassage" containername imageName  containername:被改动的容器名称 imageName：新创建的镜像
3 查看修改信息 docker diff containername

#删除容器及它的卷
docker rm -v [continaerID]|[containerName]
#获取容器的ip
docker inspect --format '{{ .NetworkSettings.IPAddress }}' 【containerID】
docker exec -ti [containerName] ip add | grep global
10 个获取 Docker 容器 IP 地址的例子（http://networkstatic.net/10-examples-of-how-to-get-docker-container-ip-address/）
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
    通过 docker commit 命令提交你对容器做出的修改，并创建一个新镜像。
    例：以交互式bash shell 的方式启动一个容器，并更新其中的软件包： docker run -it ubuntu:14.04 /bin/bash  apt-get update
        当退出容器后，容器会停止运行，但容器还在，知道你通过docker rm命令彻底删除容器。所以在删除容器前，可以提交对容器做出的修改，
        并以此创建一个新的镜像 ubuntu:update。镜像的名称为ubuntu，同时添加了一个标签update，以与ubuntu:latest镜像加以区分。
        提交你对容器做出的修改，并创建一个新镜像 docker commit [containerID] ubuntu:update
        查看创建的镜像 docker images
        查看在容器中对镜像做出的修改 docker diff [containerID]  A 表示文件或者文件夹是新增加的， C 表示文件内容有修改， D 则表示该项目已经删除。
## 将镜像和容器保存为tar文件进行共享
    对于已有镜像，可以使用docker的命令save和load命令来创建一个压缩包(tarball)；而对于容器，可以使用import和export进行导入导出操作。
    例： docker ps -a
    docker export [containerID] > update.tar
    ls
    可以在本地将容器提交为一个新镜像，但是也可以使用 docker import命令；
    docker import - update < update.tar
    docker images
# 编写Dockerfile
    以交互方式启动一个容器，在里面对容器进行修改，将修改后的容器提交为镜像，这样也
    能正常工作。但是你想自动构建镜像，并且将构建步骤与他人共享。
        要想自动构建 Docker 镜像，你需要通过一个名为 Dockerfile 的说明文件来描述镜像构建的
    步骤。这个文本文件使用一组指令来描述以下各项内容：新镜像的基础镜像，为了安装不
    同的依赖和应用程序需要执行哪些操作步骤，镜像中需要提供哪些文件，这些文件是怎么
    复制到镜像中的，要暴露哪些端口，以及在新的容器中启动时默认运行什么命令，此外还
    有一些其他的内容。
        为了对此进行说明，让我们开始编写我们的第一个 Dockerfile。从该 Dockerfile 构建的镜像
    启动新的容器时，会执行 /bin/echo 命令。在当前工作目录中创建一个名为 Dockerfile 的
    文本文件，文件内容如下所示。
    FROM ubuntu:14.04
    ENTRYPOINT ["/bin/echo"]
    FROM 指令指定了新的镜像以哪个镜像为基础开始构建。这里我们选择了 ubuntu:14.04 镜像
    作为基础镜像。ubuntu:14.04 是来自 Docker Hub 上由 Ubuntu 官方提供的镜像仓库（https://
    registry.hub.docker.com/_/ubuntu/）。 ENTRYPOINT 指令设置了从该镜像创建的容器启动时需要
    执行的命令。
        要想构建这个镜像，可以在命令行提示符下键入 docker build . 命令，如下
    所示。
        docker build .
        现在就可以基于新构建的镜像启动容器了，你需要指定刚刚创建的镜像的 ID 并指定一个
    参数（即 Hi Docker ! ），如下所示。
    $ docker run e778362ca7cf Hi Docker !
    Hi Docker !
    非常神奇，你在新容器中执行了 echo 命令！这里你基于上面由只有两行的 Dockerfile 构建
    的镜像创建了一个容器，该容器开始运行并执行了由 ENTRYPOINT 指令所定义的命令。当这
    个命令结束之后，容器的工作也即告结束并退出。如果再次运行上述命令但是不指定任何
    参数，那么就不会有任何内容回显出来，如下所示。
    $ docker run e778362ca7cf
    你也可以在 Dockerfile 文件中使用 CMD 指令。使用该指令的优点是，你可以在启动容器时，
    通过在 docker run 命令后面指定新的 CMD 参数来覆盖 Dockerfile 文件中设置的内容。让我
    们使用 CMD 指令来构建一个新镜像，如下所示。
    FROM ubuntu:14.04
    CMD ["/bin/echo" , "Hi Docker !"]
    构建这个镜像并运行它，如下所示。
    $ docker build .
    ...
    $ docker run eff764828551
    Hi Docker !
        非常神奇，你在新容器中执行了 echo 命令！这里你基于上面由只有两行的 Dockerfile 构建
    的镜像创建了一个容器，该容器开始运行并执行了由 ENTRYPOINT 指令所定义的命令。当这
    个命令结束之后，容器的工作也即告结束并退出。如果再次运行上述命令但是不指定任何
    参数，那么就不会有任何内容回显出来，如下所示。
    $ docker run e778362ca7cf
    你也可以在 Dockerfile 文件中使用 CMD 指令。使用该指令的优点是，你可以在启动容器时，
    通过在 docker run 命令后面指定新的 CMD 参数来覆盖 Dockerfile 文件中设置的内容。让我
    们使用 CMD 指令来构建一个新镜像，如下所示。
    FROM ubuntu:14.04
    CMD ["/bin/echo" , "Hi Docker !"]
    构建这个镜像并运行它，如下所示。
    $ docker build .
    ...
    $ docker run eff764828551
    Hi Docker !
        在上面的构建命令中，我们指定了当前文件夹路径。这时候 Docker 会自动使
    用刚才创建的 Dockerfile 文件。如果希望在构建镜像的时候使用在其他位置
    保存的 Dockerfile，可以使用 docker build 命令的 -f 参数来指定 Dockerfile
    文件的位置。
        上面的操作看起来与之前的例子一模一样，但是，如果在 docker run 命令后面指定一个其
    他的可执行命令，那么该命令就会被执行，而不是执行在 Dockerfile 文件中定义的 /bin/
    echo 命令，如下所示。
    $ docker run eff764828551 /bin/date
    Thu Dec 11 02:49:06 UTC 2014
        Dockerfile 是一个文本文件，它定义了一个镜像是如何构建的，以及基于该镜像创建的容
    器运行时会进行什么处理。通过 FROM 、 ENTRYPOINT 和 CMD 这三个简单的指令，你已经可
    以构建一个能完全正常工作的镜像了。当然，我们在该范例中也只是介绍了这三个指令而
    已，你可以通过阅读 Dockerfile 参考手册（https://docs.docker.com/reference/builder/）学习
    一下其他指令。
        CentOS 项目维护着很多 Dockerfile 的例子。可以在该项目的代码仓库
  （https://github.com/CentOS/CentOS-Dockerfiles）中查看这些例子，并通过运
    行其中的一些例子来加深对 Dockerfile 文件的理解。
    通过 docker build 命令的 -t 参数设置仓库名  docker build -t [imagenaem:tag] . 注意后边的 . 表示Dockerfile文件在执行命令的文件夹下。
    Dockerfile最佳实践 https://docs.docker.com/develop/develop-images/dockerfile_best-practices/
    CentOS 项 目 中 提 供 的 大 量 Dockerfile 文 件 示 例 https://github.com/CentOS/CentOS-Dockerfiles
        使用 .dockerignore 文件。在镜像构建过程中，Docker 会将 Dockerfile 所在文件夹下的内
    容（即 build context）复制到构建环境中。使用 .dockerignore 文件可以将指定文件或者
    文件夹在镜像构建时从文件复制列表中排除。如果你不使用 .dockerignore 文件，请确
    保在只有所需最小集合的文件夹下构建镜像。请参考一下 .dockerignore 的语法（https://
    docs.docker.com/reference/builder/#dockerignore-file）
        docker tag 命令的帮助信息很简洁，它显示了 Docker 镜像的
    命名规则，即如何指定正确的命名空间，可以是一个本地的镜像，或者在 Docker Hub 上，
    或者在私有 registry 上，如下所示。
    $ docker tag -h
    Usage: docker tag [OPTIONS] IMAGE[:TAG] [REGISTRYHOST/][USERNAME/]NAME[:TAG]
    Tag an image into a repository
    -f, --force=false Force
##将镜像发布到Docker Hub
    使用 Docker 命令行工具将这个镜像推送到你的公开仓库中。这需要执行以下三个步骤才
    能完成。
    (1) 通过 docker login 命令来登录 Docker Hub，这会要求你输入 Docker Hub 凭据。
    (2) 使用你 Docker Hub 上的用户名为已有镜像打标签。
    (3) 将新打完标签的镜像推送到 Docker Hub。
    登录过程会将你的 Docker Hub 凭据保存到文件 ~/.dockercfg 中，如下所示。
    $ docker login
    Username: how2dock
    Password:
    Email: how2dock@gmail.com
    Login Succeeded
    $ cat ~/.dockercfg
    {"https://index.docker.io/v1/":{"auth":"..........",
    "email":"how2dock@gmail.com"}}
    如果查看一下当前你所拥有的镜像，你会看到构建的 Flask 镜像使用了一个
    本地仓库名以及 latest 标签，如下所示。
    $ docker images
    REPOSITORY TAG IMAGE ID CREATED VIRTUAL SIZE
    flask latest 88d6464d1f42 5 days ago 354.6 MB
    ...
    要想将这个镜像推送到你的 Docker Hub 账号下，你需要通过 docker tag 命令使用你在
    Docker Hub 上的仓库为这个镜像打标签，如下所示。
    $ docker tag flask how2dock/flask
    sebimac:flask sebgoa$ docker images
    REPOSITORY TAG IMAGE ID CREATED VIRTUAL SIZE
    flask latest 88d6464d1f42 5 days ago 354.6 MB
    how2dock/flask latest 88d6464d1f42 5 days ago 354.6 MB
    现在你的 Flask 镜像有了一个 how2dock/flask 的仓库名，这也符合 Docker Hub 的仓库命名
    规则。你已经可以推送镜像了。Docker 会推送这个组成镜像的各个镜像层；如果这个镜像
    层在 Docker Hub 上已经存在了，那么这个层就会被略过。在镜像推送完成之后，你就可
    以在你的 Docker Hub 页面上看到 how2dock/flask 镜像了，并且所有人都可以通过 docker
    pull how2dock/flask 来下载这个镜像（参见图 2-1），如下所示。
    $ docker push how2dock/flask
    The push refers to a repository [how2dock/flask] (len: 1)
    Sending image list
    Pushing repository how2dock/flask (1 tags)
    511136ea3c5a: Image already pushed, skipping
    01bf15a18638: Image already pushed, skipping
    ...
    dc4a9a43bb7f: Image successfully pushed
    e394b9fbe3fa: Image successfully pushed
    3f7abcdc10d4: Image successfully pushed
    88d6464d1f42: Image successfully pushed
    Pushing tag for rev [88d6464d1f42] on
    {https://cdn-registry-1.docker.io/v1/repositories/how2dock/flask/tags/latest}
##运行私有registry
    使用 Docker Hub 非常简单。然而，你可能在数据治理方面比较关心将镜像托管在自己基础
    设施之外所带来的风险。因此，你希望在自己的基础设施之上运行自己的 Docker registry。
    2.11.2　解决方案
    使用 Docker registry 镜像（https://hub.docker.com/_/registry/）创建一个容器。这样，你就拥
    有了一个私有的 registry。
    拉取 registry 镜像并以守护方式启动一个容器。然后，你可以通过 curl 访问 http://
    localhost:5000/v2 来确认一下 registry 是否正常运行，如下所示。
    $ docker pull registry:2
    $ docker run -d -p 5000:5000 registry:2
    $ curl -i http://localhost:5000/v2/
    HTTP/1.1 200 OK
    Content-Length: 2
    Content-Type: application/json; charset=utf-8
    Docker-Distribution-Api-Version: registry/2.0
    Date: Wed, 19 Aug 2015 23:07:47 GMT
    上面的输出结果显示了 Docker registry 正在运行，其 API 版本为 v2。要想使用这个私有
    registry，需要按照正确的命名规则，为你之前创建的本地镜像（比如在范例 2.4 中创建的
    flask 镜像）打上标签。在我们的例子中，registry 运行在 http://localhost:5000 上，所以我们
    打标签的时候会使用 localhost:5000 作为前缀，并将这个镜像推送到私有 registry。也可
    以使用 Docker 主机的 IP 地址，如下所示。
    $ docker tag busybox localhost:5000/busy
    $ docker push localhost:5000/busy
    The push refers to a repository [localhost:5000/busy] (len: 1)
    8c2e06607696: Image successfully pushed
    6ce2e90b0bc7: Image successfully pushed
    cf2616975b4a: Image already exists
    latest: digest: sha256:3b5b980...a4d59f24f9c7253fce29 size: 5049
    创建和共享镜像 ｜ 55
    如果从其他计算机访问这个私有 registry，你会收到一个错误消息，提示你的 Docker 客户
    不能使用一个不安全的 registry。如果是测试环境（生产环境不建议这样操作），可以编辑
    你的 Docker 配置文件，增加 insecure-registry 选项。比如，在 Ubuntu 14.04 上编辑 /etc/
    default/docker 文件，添加如下一行。
    DOCKER_OPTS="--insecure-registry <IP_OF_REGISTRY>:5000"
    重新启动 Docker 服务（ sudo service docker restart ），然后再次访问远程私有 registry。
    （记住，需要在 registry 所在计算机之外的其他计算机上进行上述操作。）
##参考
    • Docker Hub 上的 Docker registry 主页（https://hub.docker.com/）
    • GitHub 上更丰富的文档（https://github.com/docker/distribution）
    • registry 部署说明（https://docs.docker.com/registry/deploying/）
    自动构建参考文档（https://docs.docker.com/docker-hub/builds/）
##使用Git钩子和私有registry建立本地自动构建环境
        使用 Docker Hub、GitHub 或者 Bitbucket 进行自动构建非常实用（参见范例 2.12），但是你
    可能正在使用私有 registry（比如一个本地的 hub），并且希望在向本地的 Git 项目中推送代
    码时触发 Docker 镜像构建。
    解决方案;
        创建一个 Git 的 post-commit 钩子，由它来触发一个构建并将新镜像推送到你的私有 registry。
    在你 Git 项目的根文件夹下创建一个 bash 脚本 ./git/hooks/post-commit，它的内容比较简
    单，如下所示。
    #!/bin/bash
    tag=`git log -1 HEAD --format="%h"`
    docker build -t flask:$tag /home/sebgoa/docbook/examples/flask
    使用 chmod +x .git/hooks/post-commit 命令将文件的属性设置为可执行。
    现在，每当你向 Git 项目中提交代码，bash 脚本 post-commit 都会被执行。它将会使用提
    交 SHA 的简短散列字符串作为新的 tag ，并使用指定 Dockerfile 文件触发一次构建。之后
    它就会构建一个新的名为 flask 的镜像，并使用由程序生成的标签。
    $ git commit -m "fixing hook"
    9c38962
    Sending build context to Docker daemon 3.584 kB
    Sending build context to Docker daemon
    Step 0 : FROM ubuntu:14.04
    ---> 9bd07e480c5b
    创建和共享镜像 ｜ 61
    Step 1 : RUN apt-get update
    ---> Using cache
    ---> e659c9e9ba21
    <snip>
    Removing intermediate container 05c13744c7bf
    Step 8 : CMD python /tmp/hello.py
    ---> Running in 124cd2ada52d
    ---> 9a50c7b2bee9
    Removing intermediate container 124cd2ada52d
    Successfully built 9a50c7b2bee9
    [master 9c38962] fixing hook
    1 file changed, 1 insertion(+), 1 deletion(-)
    $ docker images
    REPOSITORY TAG IMAGE ID CREATED VIRTUAL SIZE
    flask 9c38962 9a50c7b2bee9 5 days ago 354.6 MB
    尽管上面的方法能正常工作，并且它只使用了两行 bash 代码，但是如果这个构建过程需要
    花费很长时间，那么在 Git 的 post-commit 任务中进行镜像构建可能就不太切合实际了。比
    较好的方法是使用 post-commit 钩子触发一个远程的构建，然后将新镜像推送到私有 registry。
# 搭建私有镜像仓库
 docker pull registry
 docker run -d -v /opt/registry:/var/lib/registry -p 5000:5000 --restart=always --name registry registry
 配置私有镜像可信
 vi /etc/docker/daemon.json
 {"insecure-registries":["192.168.89.101:5000"]}
#向私有镜像仓库提交/拉取镜像
打标签
 docker tag tomcat:8 192.168.89.101:5000/tomcat:8
 上传
 docker push 192.168.89.101:5000/tomcat:8
 拉取
 docker pull 192.168.89.101:5000/tomcat:8
 列出镜像标签
 curl http://192.168.89.101:5000/v2/tomcat/tags/list
# 公共镜像仓库使用(Docker Hub)
 docker tag local-image:tagname reponame:tagname
 docker push reponame:tagname
 docker tag [imagename] wozhuchenfu/[registryname]:[TAG]
 docker tag [imagename] wozhuchenfu/test:v1
 登录
 docker login
 docker push wozhuchenfu/test:v1
 docker pull wozhuchenfu/test:v1
# docker网络
##查看网桥工具
    yum install bridge-utils
    brctl show
##容器访问外部网络
容器要想访问外部网络，需要本地系统的转发支持。在Linux 系统中，检查转发是否打开。
$sysctl net.ipv4.ip_forward
net.ipv4.ip_forward = 1
如果为 0，说明没有开启转发，则需要手动打开。
$sysctl -w net.ipv4.ip_forward=1
如果在启动 Docker 服务的时候设定  --ip-forward=true  , Docker 就会自动设定系统的  ip_forward  参数
为 1。
容器之间访问：
    容器之间相互访问，需要两方面的支持。
    容器的网络拓扑是否已经互联。默认情况下，所有容器都会被连接到  docker0  网桥上。
    本地系统的防火墙软件 --  iptables  是否允许通过。
##外部访问容器
docker run -idt -p IP:host_port:container_port 或 docker run -idt -p host_port:container_port
如果希望永久绑定到某个固定的IP地址，可以在Docker配置文件 /etc/default/docker 中指定 DOCKER_OPTS="--ip=IP_ADDRESS",之后重启docker服务即可生效。
# docker 网络模式
查看docker 网络模式  docker network ls
1 brige默认 docker启动后默认启动一个docker0的网卡
2 host 与宿主机共享网络
3 none 获取独立的network namespace 但不为容器进行任何网络配置
4 container 与指定的容器通用一个network namespace 网卡配置也是相同的
5 自定义
# docker容器日志查看
docker logs containername
# docker图形化界面管理
1 dockerui
2 shipyard（推荐）默认账户admin密码shipyard
##shipyard安装：
 docker pull rethinkdb
 docker pull microbox/etcd
 docker pull shipyard/docker-proxy
 docker pull swarm
 docker pull dockerclub/shipyard
 shipyard 一键部署脚本 shipyard-deploy 上传到主机
 赋予执行权限 chomd -x shipyard-deploy
 执行脚本 sh shipyard-deploy
 修改web访问端口  cat shipyard-deploy |grep 8080
        sed -i 's/8080/80/g' shipyard-deploy

# 容器监控
cAdvisor+InfluxDB+Grafana
1安装镜像：
    docker pull google/cadvisor
    docker pull influxdb
    docker pull grafana/grafana
 启动influxdb容器
 docker run -d -p 8083:8083 -p 8086:8086 --expose 8090 --expose 8099 --name influxdb influxdb
 进入influxdb容器安装cadvisor数据库
 docker exec -ti influxsrv bash
 执行 influx 命令
 创建数据库 用户
 CREATE DATABASE cadvisor
 > use cadvisor
 > CREATE USER "root" WITH PASSWORD '123456' WITH ALL PRIVILEGES
 > exit
 启动cadvisor容器
    docker run --volume=/:/rootfs:ro --volume=/var/run:/var/run:rw --volume=/sys:/sys:ro --volume=/var/lib/docker/:/var/lib/docker:ro --p=8081:8081 --detach=true --link influxdb:influxdb --name=cadvisor google/cadvisor:latest -storage_driver=influxdb -storage_driver_db=cadvisor -storage_driver_host=influxdb:8086
    通过主机IP+8081端口访问控制台
 启动grafana容器
    docker run -d -p 3000:3000 -e INFLUXDB_HOST= influxdb  -e INFLUXDB_PORT=8086 -e INFLUXDB_NAME=cadvisor -e INFLUXDB_USER=root -e INFLUXDB_PASS=123456 --link influxdb:influxdb --name grafana grafana/grafana
    通过主机IP+3000端口访问控制台，用户名密码为admin/admin
 配置grafana
# docker compose
定义和管理多容器的工具，也是一种容器的编排工具，前身是pig
##安装
 1 安装python-pip   安装命令：yum -y install epel-release
                            yum -y install python-pip
                            yum clean all
                            pip install --upgrade pip 升级
 2安装docker-compose  pip install docker-compose
#容器跨主机通信方案
1 桥接宿主机网络  使用自定义网桥连接跨主机容器。
   Docker默认的网桥是docker0.它只会在本机连接所有的容器。容器的虚拟网卡在主机上看一般叫做veth*而docker0网桥吧所有这些网卡桥接在一起
2 端口映射
3 docker网络驱动
     overlay：docker原生overlay网络
     macvlan：
4 第三方网络项目
     隧道方案：
         Flannel：
         Weave：
         OpenvSwitch：
     路由方案：
         Calico：
5 使用 svendowideit/ambassador 容器 Ambassador容器也是Docker容器，它在内部提供了转发服务。
     使用：拉取svendowideit/ambassador镜像  docker pull Ambassador
       例：在服务端主机上创建一个服务端容器redis-server     docker run -itd --name redis-server redis
          创建一个服务端Ambassador容器redis_ambassador,连接到服务端容器redis-server，并监听本地的6379端口。
          docker run -itd --name redis_ambassador --link redis-server:redis -p 6379:6379  svendowideit/ambassador
          在客户端主机上创建客户端 Ambassador容器，告诉它服务端物理主机的监听地址是tcp://x.x.x.x:6379,将本地收集到6379端口的流量转发到服务端物理主机
          docker run -itd --name redis_ambassador --expose 6379 -e REDIS_PORT_6379_TCP=tcp://x.x.x.x:6379  svendowideit/ambassador
          最后，创建一个客户端容器，进行测试，默认访问6379端口实际上访问的是服务端容器内的redis应用
          docker run -it --link redis_ambassador:redis relateiq/redis-cli
      启动 docker run -it





































































