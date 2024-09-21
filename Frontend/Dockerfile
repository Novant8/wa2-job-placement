FROM node:18 as build

WORKDIR /app

COPY . .
RUN npm ci
RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html/ui
COPY ./nginx/nginx.conf /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]