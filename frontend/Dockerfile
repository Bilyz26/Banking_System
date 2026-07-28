FROM node:20-alpine AS build
WORKDIR /workspace
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_BASE_URL=/backend
ARG VITE_OIDC_ISSUER_URL=http://localhost:9000/realms/banking
ARG VITE_OIDC_CLIENT_ID=banking-web
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_OIDC_ISSUER_URL=${VITE_OIDC_ISSUER_URL}
ENV VITE_OIDC_CLIENT_ID=${VITE_OIDC_CLIENT_ID}
RUN npm run build

FROM nginxinc/nginx-unprivileged:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget --quiet --spider http://127.0.0.1:8080/healthz || exit 1
