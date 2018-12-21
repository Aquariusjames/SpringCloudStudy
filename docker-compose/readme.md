#创建一个web项目
    一个haproxy，挂载三个web容器
1 创建一个compose-haproxy-web目录，作为项目的工作目录，并在其中分别创建两个子目录：haproxy和web。
2 web子目录  这里用一个Python程序来提供一个简单的HTTP服务，打印出访问者的IP和实际的本地IP。编写一个index.py作为服务器文件，代码为
3 生成一个临时的index.html文件，其内容会被index.py更新。
4 生成Dockerfile
5 在其中生成一个haproxy.cfg文件
6 编写docker-compose.yml
7 运行compose项目   在该目录下执行 docker-compose up
现在compose-haproxy-web目录长成的样子。
compose-haproxy-web
├── docker-compose.yml
├── haproxy
│ └── haproxy.cfg
└── web
├── Dockerfile
├── index.html
└── index.py