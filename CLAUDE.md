# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI Native ERP - A full-stack ERP system with Spring Boot backend and React frontend.

## Build & Run Commands

### Backend (ai-erp-server)

```bash
# Build
cd ai-erp-server && mvn clean package -DskipTests

# Run (development)
mvn spring-boot:run

# Run (production)
./start.sh          # Start on port 8080
./stop.sh           # Stop the application
```

### Frontend (ai-erp-web)

```bash
# Install dependencies
cd ai-erp-web && npm install

# Development server (port 3000, proxies /api to backend:8080)
npm run dev

# Build for production
npm run build
```

## Architecture

### Backend Structure (Spring Boot 3 + Java 17)

```
com.aierp/
├── entity/          # JPA entities (map to sys_* tables)
├── mapper/          # MyBatis-Plus mappers
├── service/         # Business logic layer
├── controller/      # REST API endpoints (/api/v1/*)
└── common/
    ├── config/      # Security, MyBatis config
    ├── result/      # Unified Result<T> response wrapper
    ├── exception/   # GlobalExceptionHandler + BusinessException
    └── utils/       # JwtUtil for token generation
```

Key patterns:
- **Database**: SQLite file at `./data/ai-erp.db`, schema defined in `schema.sql`
- **Auth**: JWT tokens via `/api/v1/auth/login`, validated by `JwtAuthenticationFilter`
- **Response**: All APIs return `Result<T>` with `{code, message, data}` structure
- **Multi-tenant**: All entities include `tenant_id` for data isolation

### Frontend Structure (React 18 + TypeScript + Vite)

```
src/
├── pages/           # Route-level components
├── components/      # Reusable components (Layout, etc.)
├── services/api.ts  # Axios instance + API methods
└── stores/auth.ts   # Zustand store with localStorage persistence
```

Key patterns:
- **State**: Zustand with persist middleware (auth stored in localStorage as 'auth-storage')
- **Routing**: React Router with auth guard (redirects to /login if no token)
- **API Proxy**: Vite dev server proxies `/api` to `localhost:8080`
- **UI**: Ant Design components

## Database Conventions

- Tables prefixed with `sys_` for system tables
- Common fields: `created_time`, `updated_time`, `deleted` (soft delete)
- User-table uniqueness via `uk_user_username` on `(tenant_id, username)`

## Adding New Modules

When adding a new business module (e.g., inventory, purchase):

1. **Backend**: Create entity, mapper, service, controller following existing patterns
2. **Frontend**: Add page component, update `menuItems` in `Layout.tsx`, add route in `App.tsx`
3. **Database**: Add migration script or update `schema.sql`

## Configuration Files

- Backend: `application.yml` (JWT secret, AI model config, SQLite path)
- Frontend: `vite.config.ts` (proxy config), `package.json`
