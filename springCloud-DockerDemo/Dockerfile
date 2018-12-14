#基于哪个镜像创建
FROM anImage:tag
#设置环境变量
ENV enviromentValue=value
#设置一个作者签名
MAINTAINER name emmailaddress
#执行的命令
ENTRYPOINT ['java','-jar','xxx.jar']
CMD java -jar /.../xxx.jar  --enviromentValue=value
#copy命令
COPY /filename /directory/filename
#设置对外端口
EXPOSE 8080


#构建java网站环境镜像
FROM centos:7
MAINTAINER QI
ADD jdk-8u45-linux-x64.tar.gz  /usr/local
ENV JAVA_HOME /usr/local/jdk1.8.0_45
ADD http://mirrors.tuna.tsinghua.edu.cn/apache/tomcat-8/v8.0.45/bin/apache-tomcat-8.0.45.tar.gz /usr/local
WORKDIR /usr/local/apache-tomcat-8.0.45
ENTRYPOINT ["bin/catalina.sh","run"]
EXPOSE 8080


#构建支持SSH服务的镜像
FROM centos:7
MAINTAINER QI
ENV ROOT_PASSWORD 123456
RUN yum install -y openssh-server
RUN echo $ROOT_PASSWORD |passwd --stdin root
RUN ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key
RUN ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key
CMD ["/usr/sbin/sshd","-D"]
EXPOSE 22














