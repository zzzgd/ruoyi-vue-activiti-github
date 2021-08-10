# 拉取 node 镜像，alpine 版本包最小
FROM node:12.2.0-alpine

# 设置 maintainer
LABEL maintainer = "gordon"

# 时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

# 设置 /app 为工作区
WORKDIR /app

# 将当前项目拷贝到镜像中的 /app 文件中
COPY . /app

#ARG registry=https://registry.npm.taobao.org
ARG registry=https://registry.npmjs.org/
ARG disturl=https://npm.taobao.org/dist
# 执行命令
RUN yarn config set disturl $disturl
RUN yarn config set registry $registry

RUN yarn

# 执行运行命令
ENTRYPOINT ["yarn","dev"]
CMD ["--port","8888"]
# 暴露出 8888 端口
EXPOSE 8888
