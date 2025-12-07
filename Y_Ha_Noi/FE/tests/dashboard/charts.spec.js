import { test, expect } from '@playwright/test';
import { login } from '../helpers/auth.js';

test.describe('Dashboard Charts', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    // Wait for charts to load
    await page.waitForSelector('.chart-container', { timeout: 10000 });
  });

  test('should display monthly stats chart', async ({ page }) => {
    const chartCard = page.locator('.chart-card').first();
    await expect(chartCard.locator('h3')).toContainText('Phản ánh theo tháng');
    
    // Check if chart canvas exists
    const chartCanvas = chartCard.locator('canvas');
    await expect(chartCanvas).toBeVisible({ timeout: 5000 });
  });

  test('should display department stats chart', async ({ page }) => {
    const chartCard = page.locator('.chart-card').nth(1);
    await expect(chartCard.locator('h3')).toContainText('Top 5 Phòng ban');
    
    // Check if chart canvas exists
    const chartCanvas = chartCard.locator('canvas');
    await expect(chartCanvas).toBeVisible({ timeout: 5000 });
  });

  test('should change year and reload monthly stats', async ({ page }) => {
    // Find year selector
    const yearSelect = page.locator('.el-select').first();
    await yearSelect.click();
    
    // Select different year (if available)
    const options = page.locator('.el-select-dropdown__item');
    if (await options.count() > 1) {
      await options.nth(1).click();
      // Wait for chart to update
      await page.waitForTimeout(2000);
      
      // Verify chart still visible
      const chartCanvas = page.locator('.chart-card').first().locator('canvas');
      await expect(chartCanvas).toBeVisible();
    }
  });
});

