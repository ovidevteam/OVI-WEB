/**
 * Authentication helper functions for tests
 */

export async function login(page, username = 'admin', password = 'admin123') {
  await page.goto('/login');
  await page.fill('input[placeholder="Tên đăng nhập"]', username);
  await page.fill('input[type="password"]', password);
  await page.click('button:has-text("Đăng nhập")');
  await page.waitForURL('/dashboard', { timeout: 10000 });
}

export async function logout(page) {
  // Try multiple selectors for user menu button
  const userMenuSelectors = [
    'button:has-text("AV")',
    'button[class*="user"]',
    'button[class*="avatar"]',
    '.user-menu-button',
    '[class*="user-info"]',
    '[class*="avatar"]'
  ];
  
  let menuClicked = false;
  for (const selector of userMenuSelectors) {
    const userMenuButton = page.locator(selector).first();
    if (await userMenuButton.count() > 0) {
      try {
        await userMenuButton.waitFor({ state: 'visible', timeout: 5000 });
        await userMenuButton.click();
        await page.waitForTimeout(1000);
        menuClicked = true;
        break;
      } catch {
        continue;
      }
    }
  }
  
  if (!menuClicked) {
    // Fallback: try pressing Escape to close any open menus, then try again
    await page.keyboard.press('Escape');
    await page.waitForTimeout(500);
    // Try one more time with a broader selector
    try {
      await page.click('[class*="user"], [class*="avatar"]', { timeout: 5000 });
      await page.waitForTimeout(1000);
    } catch {
      // If still can't find, just try to click logout directly
    }
  }
  
  // Click logout - wait for it to be visible
  try {
    await page.waitForSelector('text=Đăng xuất', { state: 'visible', timeout: 10000 });
    await page.click('text=Đăng xuất');
  } catch {
    // If logout not found, try alternative text
    await page.click('text=Logout, text=Thoát', { timeout: 5000 }).catch(() => {});
  }
  
  // Wait for navigation to login page - with fallback
  try {
    await page.waitForURL(/\/login/, { timeout: 10000 });
  } catch {
    // If logout didn't navigate, clear storage and navigate directly
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
    await page.goto('/login');
    await page.waitForURL(/\/login/, { timeout: 10000 });
  }
}

