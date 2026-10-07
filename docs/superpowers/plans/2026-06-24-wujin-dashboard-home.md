# Wujin Dashboard Home Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Differentiate the platform and merchant cockpit home pages so each backend shows role-specific metrics, tasks, trends, and quick actions.

**Architecture:** Keep the change scoped to the two existing `apps/web-antd/src/views/mall/home/index.vue` files. Preserve the current statistics API calls, make overview items computed so loaded data updates the cards, and add compact card sections under the overview.

**Tech Stack:** Vue 3 `<script setup>`, Vben common-ui dashboard components, Ant Design Vue cards/tags/progress, `node:test` source-level regression.

---

### Task 1: Regression Test

**Files:**
- Create: `tests/wujin-dashboard-home.test.cjs`

- [x] **Step 1: Write the failing test**

Assert that the platform source contains `平台运营总览`, `平台待关注事项`, `内容与活动趋势`, and does not contain merchant cockpit text. Assert that merchant source contains `商家经营工作台`, `经营待处理`, `近七日经营趋势`, and does not contain platform cockpit text.

- [x] **Step 2: Run test to verify it fails**

Run: `node --test tests\wujin-dashboard-home.test.cjs`

Expected: FAIL because both pages still use the same generic mall home source.

### Task 2: Platform Dashboard

**Files:**
- Modify: `Wujin-Mall-Platform-Web/apps/web-antd/src/views/mall/home/index.vue`

- [x] **Step 1: Convert top metrics to computed values**

Use existing user and order comparison responses, but label them as platform-wide membership, content, activity, and interaction health.

- [x] **Step 2: Add compact operations sections**

Add a platform title band, trend card, pending-attention list, and quick navigation for platform category, mapping, template, audit, search, and sourcing routes.

### Task 3: Merchant Dashboard

**Files:**
- Modify: `Wujin-Mall-Merchant-Web/apps/web-antd/src/views/mall/home/index.vue`

- [x] **Step 1: Convert top metrics to computed values**

Use existing user and order comparison responses, label them for sales, orders, products, and after-sale attention.

- [x] **Step 2: Add compact merchant operations sections**

Add a merchant title band, seven-day trend card, to-do list, and quick navigation for goods, orders, after-sale, coupon, comments, and merchant workbench surfaces.

### Task 4: Verification

**Files:**
- Verify both Web apps.

- [x] **Step 1: Run source regression**

Run: `node --test tests\wujin-dashboard-home.test.cjs`

Expected: PASS.

- [x] **Step 2: Build platform Web**

Run: `pnpm build:antd:sit` from `Wujin-Mall-Platform-Web`.

Expected: build exits 0.

- [x] **Step 3: Build merchant Web**

Run: `pnpm build:antd:sit` from `Wujin-Mall-Merchant-Web`.

Expected: build exits 0.
