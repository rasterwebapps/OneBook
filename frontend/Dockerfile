# ============================================================
# OneBook Frontend — Multi-stage Dockerfile
# Builds the Angular app and serves it via Nginx.
# ============================================================

# ── Stage 1: Build ───────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /workspace

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npx ng build --configuration=production

# ── Stage 2: Serve ───────────────────────────────────────────
FROM nginx:1.27-alpine
COPY --from=build /workspace/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:80/ || exit 1
