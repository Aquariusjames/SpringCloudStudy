FROM ubuntu:14.04
RUN apt-get update
#创建镜像时执行的命令 安装python
RUN apt-get install -y python
RUN apt-get insatll -y python-pip
RUN apt-get clean all
#将hello.python文件拷贝到/tmp/hello.py
ADD hello.py /tmp/hello.py
#向外部暴露的端口
EXPOSE 5000
#创建完镜像执行的命令
CMD ["python","/tmp/hello.py"]
#执行 docker build -t flask . 创建镜像名称为flask