FROM ubuntu:14.04
RUN apt-get update
#创建镜像时执行的命令 安装python
RUN apt-get install -y python
RUN apt-get insatll -y python-pip
RUN apt-get clean all
#将hello.python文件拷贝到/tmp/hello.py
ADD hello.py /tmp/hello.py
#ADD指令的功能是将主机构建环境（上下文）目录中的文件和目录、以及一个URL标记的文件 拷贝到镜像中
 #1、如果源路径是个文件，且目标路径是以 / 结尾， 则docker会把目标路径当作一个目录，会把源文件拷贝到该目录下。
 #如果目标路径不存在，则会自动创建目标路径。
 #2、如果源路径是个文件，且目标路径是不是以 / 结尾，则docker会把目标路径当作一个文件。
 #如果目标路径不存在，会以目标路径为名创建一个文件，内容同源文件；
 #如果目标文件是个存在的文件，会用源文件覆盖它，当然只是内容覆盖，文件名还是目标文件名。
 #如果目标文件实际是个存在的目录，则会源文件拷贝到该目录下。 注意，这种情况下，最好显示的以 / 结尾，以避免混淆。
 #3、如果源路径是个目录，且目标路径不存在，则docker会自动以目标路径创建一个目录，把源路径目录下的文件拷贝进来。
 #如果目标路径是个已经存在的目录，则docker会把源路径目录下的文件拷贝到该目录下。
 #4、如果源文件是个归档文件（压缩文件），则docker会自动帮解压。
#COPY指令和ADD指令功能和使用方式类似。只是COPY指令不会做自动解压工作。

#向外部暴露的端口
EXPOSE 5000
#创建完镜像执行的命令
CMD ["python","/tmp/hello.py"]
#执行 docker build -t flask . 创建镜像名称为flask
ENTRYPOINT ["python","-version"]
 #ENTRYPOINT 指令和CMD类似，它也可用户指定容器启动时要执行的命令，但如果dockerfile中也有CMD指令，CMD中的参数会被附加到ENTRYPOINT 指令的后面。
 #如果这时docker run命令带了参数，这个参数会覆盖掉CMD指令的参数，并也会附加到ENTRYPOINT 指令的后面。
 #这样当容器启动后，会执行ENTRYPOINT 指令的参数部分。
 #可以看出，相对来说ENTRYPOINT指令优先级更高。
#设置环境变量  多个不同的环境变量需要使用多个ENV指令来完成这样会导致创建多个层 \ 可以使多个环境变量值通过一个ENV指令完成设置
ENV VERSION="1.0" \
     APPROOT="/app"
#相当于docker run 命令中的-e 设置环境变量

#设置元数据，这些键值对被记录为镜像或容器的额外元数据。和docker run或docker create命令的--label选项在功能上一致
LABEL base.name="Test" \
       base.version="${VERSION}"
#指定工作目录
WORKDIR $APPROOT



#创建git镜像
FROM centos7
MAINTAINER "QI"
ENV GITTEST="git-centos7" \
    VERION="0.1" \
    APPROOT="/app"
WORKDIR $APPROOT
RUN yum install git -y
CMD ["git","-verion"]
ENTRYPOINT ["git"]
















