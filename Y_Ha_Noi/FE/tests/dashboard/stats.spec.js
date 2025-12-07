import { test, expect } from '@playwright/test';
import { login } from '../helpers/auth.js';

test.describe('Dashboard Stats Cards', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    // Wait for dashboard to load
    await page.waitForSelector('.stat-card', { timeout: 10000 });
  });

  test('should display all 4 stat cards', async ({ page }) => {
    const statCards = page.locator('.stat-card');
    await expect(statCards).toHaveCount(4);
  });

  test('should display stats with values', async ({ page }) => {
    // Check total feedbacks card
    const totalCard = page.locator('.stat-card').first();
    const totalValue = await totalCard.locator('.stat-value').textContent();
    expect(parseInt(totalValue)).toBeGreaterThanOrEqual(0);

    // Check processing card
    const processingCard = page.locator('.stat-card').nth(1);
    const processingValue = await processingCard.locator('.stat-value').textContent();
    expect(parseInt(processingValue)).toBeGreaterThanOrEqual(0);

    // Check completed card
    const completedCard = page.locator('.stat-card').nth(2);
    const completedValue = await completedCard.locator('.stat-value').textContent();
    expect(parseInt(completedValue)).toBeGreaterThanOrEqual(0);

    // Check overdue card
    const overdueCard = page.locator('.stat-card').nth(3);
    const overdueValue = await overdueCard.locator('.stat-value').textContent();
    expect(parseInt(overdueValue)).toBeGreaterThanOrEqual(0);
  });

  test('should display correct labels', async ({ page }) => {
    const labels = [
      'Tổng phản ánh tháng',
      'Đang xử lý',
      'Hoàn thành',
      'Quá hạn'
    ];

    for (let i = 0; i < labels.length; i++) {
      const card = page.locator('.stat-card').nth(i);
      await expect(card.locator('.stat-label')).toContainText(labels[i]);
    }
  });
});

