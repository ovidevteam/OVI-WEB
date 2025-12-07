# Test Suite Documentation

## Overview
This test suite uses Playwright for end-to-end browser automation testing.

## Setup
Tests are already configured. To run tests:

```bash
# Run all tests
npm test

# Run tests with UI
npm run test:ui

# Run tests in headed mode (see browser)
npm run test:headed
```

## Test Structure

```
tests/
├── auth/
│   └── login.spec.js          # Login and remember me tests
├── dashboard/
│   ├── stats.spec.js          # Dashboard stats cards tests
│   └── charts.spec.js         # Dashboard charts tests
├── feedback/
│   ├── counts.spec.js         # Feedback count consistency tests
│   ├── ratings.spec.js        # Feedback ratings tests
│   └── crud.spec.js           # Feedback CRUD operations
├── admin/
│   ├── users.spec.js          # User management CRUD
│   ├── departments.spec.js    # Department management CRUD
│   └── doctors.spec.js        # Doctor management CRUD
└── helpers/
    └── auth.js                # Authentication helper functions
```

## Test Accounts
- Admin: `admin / admin123`
- Leader: `leader / leader123`
- Handler: `handler1 / handler123`
- Receiver: `receiver / receiver123`

## Prerequisites
- Backend server running on `http://localhost:8080`
- Frontend server running on `http://localhost:5173`
- Playwright will automatically start the frontend server if not running

## Running Specific Tests

```bash
# Run only auth tests
npx playwright test tests/auth

# Run only dashboard tests
npx playwright test tests/dashboard

# Run specific test file
npx playwright test tests/auth/login.spec.js
```

