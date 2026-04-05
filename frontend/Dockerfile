# 产生阶段 - 使用 nginx
FROM nginx:alpine

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制预构建的dist文件
COPY dist /usr/share/nginx/html

# 复制 data-input.html 文件
COPY data-input.html /usr/share/nginx/html/

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
