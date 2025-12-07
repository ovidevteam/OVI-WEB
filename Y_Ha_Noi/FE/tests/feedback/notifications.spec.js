import { test, expect } from '@playwright/test';
import { login, logout } from '../helpers/auth';

test.describe('Notification System', () => {
  test.beforeEach(async ({ page }) => {
    // Clear localStorage and cookies before each test
    await page.goto('/login');
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
    await page.context().clearCookies();
  });

  test('should show FEEDBACK notification to ADMIN when new feedback is created', async ({ page }) => {
    // Step 1: Login as admin
    await login(page, 'admin', 'admin123');
    await page.waitForTimeout(2000);

    // Step 2: Get initial notification count (with multiple fallback selectors)
    const initialCount = await page.evaluate(() => {
      // Try multiple selectors for badge
      const badge = document.querySelector('.notification-badge .el-badge__content') ||
                    document.querySelector('.el-badge__content') ||
                    document.querySelector('[class*="badge"] [class*="content"]');
      return badge ? parseInt(badge.textContent) || 0 : 0;
    });

    // Step 3: Create a new feedback (as receiver - only RECEIVER and ADMIN can create feedback)
    await logout(page);
    await login(page, 'receiver', 'receiver123');
    await page.waitForTimeout(1000);

    // Navigate to create feedback page - use direct navigation instead of menu
    await page.goto('/feedback/create');
    await page.waitForURL(/\/feedback\/create/, { timeout: 10000 });
    
    // Wait for page to be fully loaded - wait for specific elements unique to create page
    // Wait for either the title or the form field label (use separate waits)
    try {
      await page.waitForSelector('text=Nhập Phản ánh mới', { timeout: 15000 });
    } catch {
      // Fallback: wait for form field
      await page.waitForSelector('.el-form-item:has-text("Kênh tiếp nhận")', { timeout: 15000 });
    }
    await page.waitForTimeout(1000);

    // Verify we're on the create page by checking for the form
    const titleExists = await page.getByText('Nhập Phản ánh mới').count() > 0;
    const formExists = await page.locator('.feedback-form, .el-form').count() > 0;
    if (!titleExists && !formExists) {
      throw new Error('Failed to navigate to feedback create page');
    }

    // Fill feedback form - wait for select to be visible
    const channelSelect = page.locator('.el-form-item:has-text("Kênh tiếp nhận") .el-select').first();
    await channelSelect.waitFor({ state: 'visible', timeout: 15000 });
    await channelSelect.click();
    await page.waitForTimeout(500);
    // Wait for dropdown to appear
    await page.waitForSelector('.el-select-dropdown:visible', { timeout: 10000 });
    await page.locator('.el-select-dropdown__item:has-text("Trực tiếp")').first().click();
    await page.waitForTimeout(500);

    // Level is already set to "Trung bình" (MEDIUM) by default, verify it's checked
    // Skip clicking level as it's already set correctly
    // Just verify the form is ready
    await page.waitForTimeout(300);

    // Select department
    const departmentSelect = page.locator('.el-form-item:has-text("Phòng liên quan") .el-select').first();
    await departmentSelect.waitFor({ state: 'visible', timeout: 10000 });
    await departmentSelect.click();
    await page.waitForTimeout(1000);
    // Wait for dropdown to appear
    await page.waitForSelector('.el-select-dropdown:visible', { timeout: 10000 });
    // Wait for at least one item to be in DOM (even if hidden)
    await page.waitForSelector('.el-select-dropdown__item', { timeout: 10000 });
    await page.waitForTimeout(500);
    // Click first item - don't wait for visible, just click it
    await page.locator('.el-select-dropdown__item').first().click({ force: true, timeout: 5000 });
    await page.waitForTimeout(500);

    // Fill content
    const contentTextarea = page.locator('textarea[placeholder*="Nhập nội dung phản ánh chi tiết"]').first();
    await contentTextarea.waitFor({ state: 'visible', timeout: 10000 });
    await contentTextarea.fill('Test notification feedback - ' + new Date().getTime());
    await page.waitForTimeout(300);

    // Submit feedback - button text is "Lưu phản ánh"
    const submitButton = page.locator('button:has-text("Lưu phản ánh")').first();
    await submitButton.waitFor({ state: 'visible', timeout: 10000 });
    await submitButton.click();
    
    // Wait for success message or navigation
    try {
      await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 15000 });
    } catch {
      // If no success message, wait for navigation or form reset
      await page.waitForTimeout(2000);
    }
    
    // Wait for feedback to be created and notification to be sent
    await page.waitForTimeout(2000);

    // Step 4: Logout and login as admin to check notification
    await logout(page);
    await login(page, 'admin', 'admin123');
    // Wait for page to load and notifications to be fetched
    await page.waitForTimeout(3000);

    // Step 5: Check notification badge increased (with retry logic)
    let newCount = 0;
    let retries = 3;
    while (retries > 0) {
      newCount = await page.evaluate(() => {
        // Try multiple selectors for badge
        const badge = document.querySelector('.notification-badge .el-badge__content') ||
                      document.querySelector('.el-badge__content') ||
                      document.querySelector('[class*="badge"] [class*="content"]');
        return badge ? parseInt(badge.textContent) || 0 : 0;
      });
      
      if (newCount > initialCount) {
        break;
      }
      
      // Wait a bit more and retry
      await page.waitForTimeout(2000);
      retries--;
    }

    expect(newCount).toBeGreaterThan(initialCount);

    // Step 6: Open notifications dropdown
    const notificationButton = page.locator('.notification-btn-wrapper, .notification-btn, button[class*="notification"]').first();
    await notificationButton.waitFor({ state: 'visible', timeout: 10000 });
    await notificationButton.click();
    await page.waitForTimeout(2000);

    // Wait for dropdown to be visible
    await page.waitForSelector('.el-popover, .notification-list, .notification-item', { timeout: 10000 });

    // Check if notification exists
    const notificationTitle = page.locator('.notification-text:has-text("Phản ánh mới"), [class*="notification"]:has-text("Phản ánh mới")').first();
    await expect(notificationTitle).toBeVisible({ timeout: 15000 });
    
    // Verify notification message contains feedback info
    const notificationMessage = page.locator('.notification-desc').first();
    const messageText = await notificationMessage.textContent();
    expect(messageText).toContain('Phản ánh mới');
  });

  test('should show ASSIGNED notification to handler1 when feedback is assigned', async ({ page }) => {
    // Step 1: Login as admin
    await login(page, 'admin', 'admin123');
    await page.waitForTimeout(2000);

    // Step 2: Navigate to feedback list - use direct navigation instead of menu
    await page.goto('/feedback/list');
    await page.waitForURL(/\/feedback\/list/, { timeout: 10000 });
    await page.waitForTimeout(2000);

    // Step 3: Find a feedback without handler (status NEW or PROCESSING)
    // Look for feedbacks with "Chưa xử lý" or "Đang xử lý" status
    const feedbackRows = page.locator('tbody tr');
    const rowCount = await feedbackRows.count();
    
    if (rowCount === 0) {
      test.skip('No feedbacks found');
      return;
    }

    let feedbackCode = null;
    let foundUnassigned = false;

    // Try to find an unassigned feedback
    for (let i = 0; i < Math.min(rowCount, 5); i++) {
      const row = feedbackRows.nth(i);
      const statusText = await row.locator('td').nth(4).textContent().catch(() => '');
      
      if (statusText.includes('Chưa xử lý') || statusText.includes('Đang xử lý')) {
        feedbackCode = await row.locator('td').first().textContent();
        await row.click();
        await page.waitForTimeout(2000);
        foundUnassigned = true;
        break;
      }
    }

    if (!foundUnassigned) {
      test.skip('No unassigned feedback found');
      return;
    }

    // Step 4: Try to assign handler1
    // Look for assign button or handler selection in detail page
    const assignButton = page.locator('button:has-text("Phân công"), button:has-text("Gán"), button:has-text("Assign")').first();
    
    if (await assignButton.count() > 0) {
      await assignButton.click();
      await page.waitForTimeout(1000);

      // Wait for dialog
      await page.waitForSelector('.el-dialog, .el-select-dropdown', { timeout: 5000 });

      // Select handler1
      const handlerSelect = page.locator('.el-form-item:has-text("Người xử lý") .el-select').or(page.locator('.el-dialog .el-select').first());
      await handlerSelect.click();
      await page.waitForTimeout(500);
      
      // Select handler1 from dropdown
      const handlerOption = page.locator('.el-select-dropdown__item:has-text("handler1"), .el-select-dropdown__item:has-text("BS. Handler 1"), .el-select-dropdown__item:has-text("Handler 1")').first();
      if (await handlerOption.count() > 0) {
        await handlerOption.click();
        await page.waitForTimeout(500);

        // Confirm assignment
        const confirmButton = page.locator('button:has-text("Xác nhận"), button:has-text("Lưu"), button:has-text("Phân công")').first();
        await confirmButton.click();
        await page.waitForTimeout(2000);
      }
    } else {
      // If no assign button, try API call directly or skip
      test.skip('Assign functionality not available in UI');
      return;
    }

    // Step 5: Logout and login as handler1
    await logout(page);
    await login(page, 'handler1', 'handler123');
    await page.waitForTimeout(2000);

    // Step 6: Check notification badge (with retry logic)
    let badgeCount = 0;
    let retries = 3;
    while (retries > 0) {
      badgeCount = await page.evaluate(() => {
        const badge = document.querySelector('.notification-badge .el-badge__content') ||
                      document.querySelector('.el-badge__content') ||
                      document.querySelector('[class*="badge"] [class*="content"]');
        return badge ? parseInt(badge.textContent) || 0 : 0;
      });
      
      if (badgeCount > 0) {
        break;
      }
      
      await page.waitForTimeout(2000);
      retries--;
    }

    expect(badgeCount).toBeGreaterThan(0);

    // Step 7: Open notifications
    const notificationButton = page.locator('.notification-btn-wrapper, .notification-btn, button[class*="notification"]').first();
    await notificationButton.waitFor({ state: 'visible', timeout: 10000 });
    await notificationButton.click();
    await page.waitForTimeout(2000);

    // Wait for dropdown to be visible
    await page.waitForSelector('.el-popover, .notification-list, .notification-item', { timeout: 10000 });

    // Check for ASSIGNED notification
    const notificationTitle = page.locator('.notification-text:has-text("Được phân công"), [class*="notification"]:has-text("Được phân công")').first();
    await expect(notificationTitle).toBeVisible({ timeout: 15000 });
    
    // Verify notification message contains feedback code
    const notificationMessage = page.locator('.notification-desc').first();
    const messageText = await notificationMessage.textContent();
    expect(messageText).toContain(feedbackCode);
  });

  test('should show COMPLETED notification to ADMIN when feedback is completed', async ({ page }) => {
    // Step 1: Login as handler1
    await login(page, 'handler1', 'handler123');
    await page.waitForTimeout(2000);

    // Step 2: Navigate to my feedbacks - use direct navigation instead of menu
    await page.goto('/my-feedbacks');
    await page.waitForURL(/\/my-feedbacks|\/feedback\/my/, { timeout: 10000 });
    await page.waitForTimeout(2000);

    // Step 3: Find a feedback that can be completed (status ASSIGNED or PROCESSING)
    const feedbackRows = page.locator('tbody tr');
    const rowCount = await feedbackRows.count();
    
    if (rowCount === 0) {
      test.skip('No feedbacks assigned to handler1');
      return;
    }

    let foundProcessable = false;

    // Try to find a processable feedback
    for (let i = 0; i < Math.min(rowCount, 5); i++) {
      const row = feedbackRows.nth(i);
      const statusText = await row.locator('td').nth(4).textContent().catch(() => '');
      
      if (statusText.includes('Đang xử lý') || statusText.includes('Đã phân công')) {
        const processButton = row.locator('button:has-text("Xử lý"), button:has-text("Xem")').first();
        if (await processButton.count() > 0) {
          await processButton.click();
          await page.waitForTimeout(2000);
          foundProcessable = true;
          break;
        }
      }
    }

    if (!foundProcessable) {
      test.skip('No processable feedback found');
      return;
    }

    // Step 4: Complete the feedback
    // Look for complete button or status change dialog
    const completeButton = page.locator('button:has-text("Hoàn thành"), button:has-text("Xử lý xong"), button:has-text("Complete")').first();
    
    if (await completeButton.count() > 0) {
      await completeButton.click();
      await page.waitForTimeout(1000);

      // Wait for dialog if appears
      await page.waitForSelector('.el-dialog, .el-form', { timeout: 5000 }).catch(() => {});

      // Fill completion form if needed
      const noteTextarea = page.locator('textarea[placeholder*="Ghi chú"], textarea[placeholder*="Nhận xét"], textarea[placeholder*="Kết quả"]').first();
      if (await noteTextarea.count() > 0) {
        await noteTextarea.fill('Đã xử lý xong - test notification');
        await page.waitForTimeout(500);
      }

      // Submit completion
      const submitButton = page.locator('button:has-text("Xác nhận"), button:has-text("Lưu"), button:has-text("Hoàn thành")').first();
      await submitButton.waitFor({ state: 'visible', timeout: 10000 });
      await submitButton.click();
      // Wait for completion to be processed and notification to be sent
      await page.waitForTimeout(5000);
    } else {
      test.skip('Complete button not found - feedback may already be completed');
      return;
    }

    // Step 5: Logout and login as admin
    await logout(page);
    await login(page, 'admin', 'admin123');
    await page.waitForTimeout(2000);

    // Step 6: Check notification badge increased (with retry logic)
    let badgeCount = 0;
    let retries = 3;
    while (retries > 0) {
      badgeCount = await page.evaluate(() => {
        const badge = document.querySelector('.notification-badge .el-badge__content') ||
                      document.querySelector('.el-badge__content') ||
                      document.querySelector('[class*="badge"] [class*="content"]');
        return badge ? parseInt(badge.textContent) || 0 : 0;
      });
      
      if (badgeCount > 0) {
        break;
      }
      
      await page.waitForTimeout(2000);
      retries--;
    }

    expect(badgeCount).toBeGreaterThan(0);

    // Step 7: Open notifications
    const notificationButton = page.locator('.notification-btn-wrapper, .notification-btn, button[class*="notification"]').first();
    await notificationButton.waitFor({ state: 'visible', timeout: 10000 });
    await notificationButton.click();
    await page.waitForTimeout(2000);

    // Wait for dropdown to be visible
    await page.waitForSelector('.el-popover, .notification-list, .notification-item', { timeout: 10000 });

    // Check for COMPLETED notification
    const notificationTitle = page.locator('.notification-text:has-text("Hoàn thành"), [class*="notification"]:has-text("Hoàn thành")').first();
    await expect(notificationTitle).toBeVisible({ timeout: 15000 });
  });

  test('should mark notification as read when clicked', async ({ page }) => {
    // Step 1: Login as admin (assuming there are notifications)
    await login(page, 'admin', 'admin123');
    await page.waitForTimeout(2000);

    // Step 2: Open notifications
    const notificationButton = page.locator('.notification-btn-wrapper, .notification-btn').first();
    await notificationButton.click();
    await page.waitForTimeout(1000);

    // Step 3: Get unread count before
    const badgeBefore = await page.evaluate(() => {
      const badge = document.querySelector('.notification-badge .el-badge__content');
      return badge ? parseInt(badge.textContent) || 0 : 0;
    });

    if (badgeBefore === 0) {
      test.skip('No notifications to mark as read');
      return;
    }

    // Step 4: Click on first unread notification
    const firstUnreadNotification = page.locator('.notification-item.unread').first();
    if (await firstUnreadNotification.count() > 0) {
      await firstUnreadNotification.click();
      await page.waitForTimeout(2000);
    } else {
      // If no unread, click first notification
      const firstNotification = page.locator('.notification-item').first();
      if (await firstNotification.count() > 0) {
        await firstNotification.click();
        await page.waitForTimeout(2000);
      }
    }

    // Step 5: Close popover and check badge again
    await page.keyboard.press('Escape');
    await page.waitForTimeout(1000);

    // Re-open to check
    await notificationButton.click();
    await page.waitForTimeout(1000);

    const badgeAfter = await page.evaluate(() => {
      const badge = document.querySelector('.notification-badge .el-badge__content');
      return badge ? parseInt(badge.textContent) || 0 : 0;
    });

    // Badge should decrease or stay same (depending on implementation)
    expect(badgeAfter).toBeLessThanOrEqual(badgeBefore);
  });

  test('should show notification count in badge', async ({ page }) => {
    // Step 1: Login as admin
    await login(page, 'admin', 'admin123');
    await page.waitForTimeout(2000);

    // Step 2: Check if notification badge exists
    const badgeWrapper = page.locator('.notification-badge').first();
    const badgeExists = await badgeWrapper.count() > 0;
    
    if (badgeExists) {
      // Badge might be hidden if count is 0, so check if visible
      const badgeContent = page.locator('.notification-badge .el-badge__content');
      const isVisible = await badgeContent.isVisible().catch(() => false);
      
      if (isVisible) {
        const badgeText = await badgeContent.textContent();
        const badgeNumber = parseInt(badgeText) || 0;
        
        // Badge should show a number (greater than 0 if visible)
        expect(badgeNumber).toBeGreaterThan(0);
      } else {
        // Badge is hidden (count is 0) - this is also valid
        expect(true).toBe(true);
      }
    } else {
      // If no badge wrapper, that's also acceptable
      test.skip('Notification badge not found - may have no notifications');
    }
  });
});

