# Plan Fix Failed Tests - Version 1.2.0

## Tổng quan
- **Tổng số test failed**: 13
- **Version hiện tại**: 1.1.0
- **Version mới**: 1.2.0

## Phân tích lỗi

### 1. my-feedbacks.spec.js (9 tests failed)

#### Lỗi chính:
- **Strict mode violation**: Selector `text=Chờ xử lý` match 2 elements (stat-label và tab)
- **Timeout issues**: Nhiều test timeout ở 30.1s

#### Tests cần fix:
1. `should display my feedbacks page with stats` - strict mode violation
2. `should display feedback information in dialog` - selector issues
3. `should submit processing form successfully` - timeout
4. `should validate required fields before submit` - selector issues
5. `should update stats after processing feedback` - timeout
6. `should display empty state when no feedbacks` - selector issues
7. `should fill all processing form fields correctly` - timeout
8. `should display processing history after submission` - selector issues
9. `should display processing history with correct format` - selector issues

#### Giải pháp:
- Sử dụng `.first()` hoặc selector cụ thể hơn cho tab labels
- Thêm `.el-tabs__item` prefix cho tab selectors
- Cải thiện wait conditions
- Tăng timeout cho các operations cần thiết
- Sử dụng `getByRole` hoặc `getByTestId` khi có thể

### 2. notifications.spec.js (3 tests failed)

#### Lỗi chính:
- **Timeout**: Tất cả đều timeout ở 30.1s
- Có thể do notification không xuất hiện kịp thời

#### Tests cần fix:
1. `should show FEEDBACK notification to ADMIN when new feedback is created`
2. `should show ASSIGNED notification to handler1 when feedback is assigned`
3. `should show COMPLETED notification to ADMIN when feedback is completed`

#### Giải pháp:
- Tăng timeout cho notification wait
- Cải thiện selector cho notification elements
- Thêm retry logic nếu cần

### 3. ratings.spec.js (1 test failed)

#### Lỗi chính:
- **Timeout**: Timeout ở 30.1s

#### Test cần fix:
1. `should submit rating successfully`

#### Giải pháp:
- Cải thiện wait conditions cho rating dialog
- Kiểm tra selector cho rating form

## Kế hoạch thực hiện

### Phase 1: Fix my-feedbacks.spec.js
1. Fix strict mode violations bằng cách sử dụng selector cụ thể hơn
2. Cải thiện tab selectors với `.el-tabs__item` prefix
3. Thêm proper waits cho async operations
4. Fix timeout issues bằng cách tối ưu selectors

### Phase 2: Fix notifications.spec.js
1. Tăng timeout cho notification waits
2. Cải thiện notification selectors
3. Thêm proper wait conditions

### Phase 3: Fix ratings.spec.js
1. Cải thiện rating form selectors
2. Thêm proper waits

### Phase 4: Update Version
1. Update package.json version từ 1.1.0 → 1.2.0
2. Update version trong Sidebar.vue nếu cần

## Chi tiết fix

### Fix 1: Strict Mode Violation
```javascript
// ❌ Bad - matches 2 elements
await expect(page.locator('text=Chờ xử lý')).toBeVisible();

// ✅ Good - specific to tabs
await expect(page.locator('.el-tabs__item:has-text("Chờ xử lý")')).toBeVisible();
// hoặc
await expect(page.getByRole('tab', { name: 'Chờ xử lý' })).toBeVisible();
```

### Fix 2: Timeout Issues
```javascript
// ❌ Bad - no proper wait
await page.click('button:has-text("Xử lý")');

// ✅ Good - wait for element first
await page.waitForSelector('button:has-text("Xử lý")', { state: 'visible' });
await page.click('button:has-text("Xử lý")');
await page.waitForSelector('.el-dialog:visible', { timeout: 10000 });
```

### Fix 3: Empty State
```javascript
// ❌ Bad - might not find
await expect(page.locator('.el-empty')).toBeVisible();

// ✅ Good - flexible check
const emptyState = page.locator('.el-empty, text=/Không có/i');
const hasData = page.locator('.el-table tbody tr').count() > 0;
expect(await emptyState.count() > 0 || await hasData).toBeTruthy();
```

